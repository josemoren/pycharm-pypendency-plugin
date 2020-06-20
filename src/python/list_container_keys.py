#!/usr/bin/env /usr/local/bin/python3

from pypendency.builder import container_builder
from pypendency.loaders.yaml_loader import YamlLoader
from glob import glob
import sys
import re


def parse_py_file(file_name):
    with open(file_name) as fp:
        for cnt, line in enumerate(fp):
            strings = re.findall(r"\"(.*?)\"", line)
            for str in strings:
                if "." in str: 
                    print(str)


base_path = sys.argv[1]

yaml_files = [f for f in glob(f"{base_path}/src/_dependency_injection/**/[!_]*.yaml", recursive=True)]
yaml_files += [f for f in glob(f"{base_path}/src/*/_dependency_injection/**/[!_]*.yaml", recursive=True)]
for yaml in yaml_files:
    try:
        YamlLoader(container_builder).load(yaml)
    except Exception as e:
        pass

py_files = [f for f in glob(f"{base_path}/**/_dependency_injection/**/[!_]*.py", recursive=True)]
for py_file in py_files:
    parse_py_file(py_file)

for key in list(container_builder._service_mapping.keys()):
    print(key)

