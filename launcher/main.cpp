#include <iostream>
#include "fstream"
#include <cstring>
#include <dirent.h>


const std::string CONFIG_FILE = "launcher.config";
#ifdef __unix__
const std::string DEFAULT_CONFIG =
        "java_bin: java;\n"
        "jar: 1Hoever.jar;\n"
        "token: enter_token_here;\n"
        "bot_config: bot_config.json;\n"
        "updater: 1;\n"
        "git_repo: https://github.com/TureBentzin/1Hoever.git;\n"
        "bot_debug: 0;\n";
#else
const std::string DEFAULT_CONFIG =
        "java_bin: java;\n"
        "jar: 1Hoever.jar;\n"
        "token: enter_token_here;\n"
        "bot_config: bot_config.json;\n"
        "updater: 0;\n"
        "git_repo: https://github.com/TureBentzin/1Hoever.git;\n"
        "bot_debug: 0;\n";
#endif

bool debug = false;

struct Config {
    std::string java_bin;
    std::string jar;
    std::string token;
    std::string bot_config;
    bool updater = true;
    std::string git_repo;
    bool bot_debug = false;
};

bool checkForCommand(std::string command) {
    std::string command_string = "which " + command + " > /dev/null";
    return !system(command_string.c_str());
}

int main(int argc, char **argv) {

    {
        //check if -d flag is present
        for (int i = 0; i < argc; i++) {
            if (strcmp(argv[i], "-d") == 0) {
                debug = true;
                std::cout << "Debug mode enabled!" << std::endl << std::endl;
                std::cout << "Running in debug mode will print the token to this console. This is a huge security risk!"
                          << std::endl;
            }
        }
    }

    std::cout << "Welcome to the 1Hoever Launcher!" << std::endl;
    std::cerr << "This is an experimental launcher! Usage in production is not recommended!" << std::endl;
    //flushall();
    bool enable_updater = true;
    restart:
    std::cout << "Reading launcher configuration from file: " << CONFIG_FILE << std::endl;
// check if the file exists and is readable. if not, create it.
    Config config = {};
    attempt_read_config:
    std::fstream file(CONFIG_FILE, std::ios::in | std::ios::out);
    if (!file.is_open()) {
        std::cout << "File not found, creating new file" << std::endl;
        file.open(CONFIG_FILE, std::ios::out);
        file << DEFAULT_CONFIG << std::endl;
        file.close();
        std::cout << "File created, please fill in the configuration and restart the launcher!" << std::endl;
        return 0;
    } else {
        std::cout << "File found, reading contents..." << std::endl;
        std::string line;
        while (std::getline(file, line)) {
            if (debug) std::cout << line << std::endl;
            //parse the line
            //token
            if (line.find("token: ") != std::string::npos) {
                config.token = line.substr(7, line.length() - 8);
                if (debug) std::cout << "parsed: token: " << config.token << std::endl;
            }
            //bot_config
            if (line.find("bot_config: ") != std::string::npos) {
                config.bot_config = line.substr(12, line.length() - 13);
                if (debug) std::cout << "parsed: bot_config: " << config.bot_config << std::endl;
            }
            //updater
            if (line.find("updater: ") != std::string::npos) {
                config.updater = line.substr(9, line.length() - 10) == "1";
                if (debug) std::cout << "parsed: updater: " << config.updater << std::endl;
            }
            //git_repo
            if (line.find("git_repo: ") != std::string::npos) {
                config.git_repo = line.substr(10, line.length() - 11);
                if (debug) std::cout << "parsed: git_repo: " << config.git_repo << std::endl;
            }
            //bot_debug
            if (line.find("bot_debug: ") != std::string::npos) {
                config.bot_debug = line.substr(11, line.length() - 12) == "1";
                if (debug) std::cout << "parsed: bot_debug: " << config.bot_debug << std::endl;
            }
            //jar
            if (line.find("jar: ") != std::string::npos) {
                config.jar = line.substr(5, line.length() - 6);
                if (debug) std::cout << "parsed: jar: " << config.jar << std::endl;
            }
            //java_bin
            if (line.find("java_bin: ") != std::string::npos) {
                config.java_bin = line.substr(10, line.length() - 11);
                if (debug) std::cout << "parsed: java_bin: " << config.java_bin << std::endl;
            }
        }
        file.close();
    }

    std::cout << "Configuration read successfully!" << std::endl;
    //if updater is enabled, download the latest sucessful build from the git repo
    if (config.updater && enable_updater) {

        std::cout << "Updater is enabled, downloading latest build from: " << config.git_repo << std::endl;
        //if the source folder exists, delete it
        std::fstream source_folder("source", std::ios::in);
        std::string command = "git clone " + config.git_repo + " source";
        if (debug) std::cout << "Running command: " << command << std::endl;
        bool source_exists = false;
        DIR* dir = opendir("source");
        if(dir) {
            closedir(dir);
            source_exists = true;
        }
        if (checkForCommand("git")) {
            int exit_code;
            if(!source_exists) {
                exit_code = system(command.c_str());
                if (exit_code != 0) {
                    std::cout
                            << "Failed to download the latest build! Please make sure the git repo is correct and you are authorized to clone it!"
                            << std::endl;
                    goto update_failed;
                }
            }else {
                std::cout << "Source folder already exists! Pulling latest changes..." << std::endl;
                std::string pull_command = "cd source && git pull";
                if (debug) std::cout << "Running command: " << pull_command << std::endl;
                exit_code = system(pull_command.c_str());
                if (exit_code != 0) {
                    std::cout << "Failed to pull the latest changes! Please make sure the git repo is correct and you are authorized to pull it!" << std::endl;
                    goto update_failed;
                }
            }
            {
                //extract commit hash
                std::string commit_hash_command = "cd source && git rev-parse HEAD";
                if (debug) std::cout << "Running command: " << commit_hash_command << std::endl;
                FILE *commit_hash_file = popen(commit_hash_command.c_str(), "r");
                if (!commit_hash_file) {
                    std::cout << "Failed to extract commit hash!" << std::endl;
                } else {
                    char commit_hash[100];
                    fgets(commit_hash, 100, commit_hash_file);
                    std::cout << "Latest build downloaded! Commit hash: " << commit_hash << std::endl;
                    pclose(commit_hash_file);
                }
                //cd back
                std::string cd_back_command = "cd ..";
                if (debug) std::cout << "Running command: " << cd_back_command << std::endl;
                exit_code = system(cd_back_command.c_str());
                if (exit_code != 0) {
                    std::cout << "Failed to cd back??!" << std::endl;
                    goto update_failed;
                }
            }
            if (checkForCommand("mvn")) {
                std::string build_command = "mvn -f source clean test install";
                if (debug) std::cout << "Running command: " << build_command << std::endl;
                exit_code = system(build_command.c_str());
                if (exit_code != 0) {
                    std::cout << "Failed to build the latest build! Please make sure the build is correct."
                                 " Please open an issue if this error persists over more then 40h!" << std::endl;
                    goto update_failed;
                }
                std::cout << "Build successful!" << std::endl;
                {
                    //backup the old jar file
                    std::string backup_command = "mv " + config.jar + " " + config.jar + ".backup";
                    if (debug) std::cout << "Running command: " << backup_command << std::endl;
                    exit_code = system(backup_command.c_str());
                    if (exit_code != 0) {
                        std::cout << "Failed to backup the old jar file!" << std::endl;
                        goto update_failed;
                    }
                }
                std::string copy_command = "cp source/target/Hoever.jar " + config.jar;
                if (debug) std::cout << "Running command: " << copy_command << std::endl;
                exit_code = system(copy_command.c_str());
                if (exit_code != 0) {
                    std::cout << "Failed to copy the jar file!" << std::endl;
                    std::cout << "Restoring backup..." << std::endl;
                    std::string restore_command = "mv " + config.jar + ".backup " + config.jar;
                    if (debug) std::cout << "Running command: " << restore_command << std::endl;
                    exit_code = system(restore_command.c_str());
                    if (exit_code != 0) {
                        std::cout << "Failed to restore the backup!" << std::endl;
                        std::cout << "Please restore the backup manually!" << std::endl;
                    }
                    goto update_failed;
                }
                std::cout << "Jar file copied successfully!" << std::endl;
                std::cout << "Currently the launcher does not allow automatic updating of itself."
                             " However the updater will try to build the newest version at source/launcher."
                          << std::endl;
                {
                    //launcher build
                    std::string build_command = "cmake -S source/launcher -B source/launcher/build";
                    if (debug) std::cout << "Running command: " << build_command << std::endl;
                    exit_code = system(build_command.c_str());
                    if (exit_code != 0) {
                        std::cout
                                << "Failed to build the launcher! Is CMake installed correctly? Is your Toolchain working?"
                                << std::endl;
                        goto update_failed;
                    }
                }
            } else {
                std::cout << "Maven not found! Please make sure maven is installed!" << std::endl;
                goto update_failed;
            }
        } else {
            std::cout << "Git not found! Please make sure git is installed!" << std::endl;
            goto update_failed;
        }
    }
    update_failed:

    //check if the jar file exists
    std::fstream jar_file(config.jar, std::ios::in);
    if (!config.jar.ends_with(".jar")) {
        std::cout << "Jar file must end with .jar! Please make sure 1Hoever is installed correctly!" << std::endl;
        return 1;
    }
    if (!jar_file.is_open()) {
        std::cout << "Jar file not found! Please make sure 1Hoever is installed correctly!" << std::endl;
        return 1;
    }
    jar_file.close();
    std::cout << "Jar file found!" << std::endl;
    //build command to run the jar file
    std::string command = config.java_bin + " -jar " + config.jar + " " + config.token + " " + config.bot_config + " " +
                          (config.bot_debug ? "-d" : "");
    if (debug) std::cout << "Running command: " << command << std::endl;
    //run the command
    int exit_code = system(command.c_str());
#ifdef __unix__
    exit_code = exit_code >> 8;
#endif
    std::cout << "The bot has exited with exit code: " << exit_code << std::endl;
    /*
     * Exit codes of the bot:
     * -1: unknown error (exit without restart)
     * 0: normal exit (dont restart)
     * 1: restart without update
     * 2: restart with update
     * 3: error (restart with update)
     * 4 +: unrecoverable error (dont restart)
     */
    if (exit_code == 0) {
        std::cout << "The bot has exited normally!" << std::endl;
        return exit_code;
    } else if (exit_code == 1) {
        std::cout << "The bot has requested a restart without update!" << std::endl;
        enable_updater = false;
        goto restart;
    } else if (exit_code == 2) {
        std::cout << "The bot has requested a restart with update!" << std::endl;
        enable_updater = true;
        goto restart;
    } else if (exit_code == 3) {
        std::cout << "The bot has exited with an error and requested a restart!" << std::endl;
        enable_updater = true;
        goto restart;
    } else if (exit_code == 4) {
        std::cout << "The bot has exited with an unrecoverable error!" << std::endl;
        return exit_code;
    } else {
        std::cout << "The bot has exited with an unknown error! (" << exit_code << ")" << std::endl;
        return -1;
    }


    return 0;
}
