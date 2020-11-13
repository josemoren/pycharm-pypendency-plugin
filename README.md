# pycharm-pypendency-plugin
PyCharm Plugin for Pypendency

The plugin provides navigation for pypendency files.

### Installation
Use the option *'install plugin from disk'* in pycharm to install the file in the `build/distributions` folder.

### How it works
The plugin adds the following entry to the `Navigate` Menu:

![pypendency_navigation](https://user-images.githubusercontent.com/33334531/99075928-95110f00-25ba-11eb-9a52-b840f7505f3b.png)

This is also available as an action:

![pypendency_action](https://user-images.githubusercontent.com/33334531/99076020-c1c52680-25ba-11eb-9d96-7c369c465eb1.png)

The action should be triggered when the cursor is placed upon the name of the class for which we want to find the pypendency definition file.

The action checks for the existence of a `.py` or `.yaml` file in a `_dependency_injection` folder with the same relative path as the current file.
The `_dependency_injection` folder **must be present** for the pluging to work. It must be created manually.

If the pypendency file does not exist a choice is given to create the pypendency file. 

![pypendency_choice](https://user-images.githubusercontent.com/33334531/99076339-444de600-25bb-11eb-92d8-9453cc2a5e25.png)

The folder structure is automatically created inside `_dependency_injection` to mimic the current file's path.
