#!/usr/bin/env /usr/local/bin/python3

from pypendency.builder import container_builder
from pypendency.definition import Definition
from pypendency.loaders.yaml_loader import YamlLoader
from pypendency.loaders.py_loader import PyLoader
from glob import glob
import sys
import re


# folders = [s for s in glob(f"{sys.argv[1]}/**/_dependency_injection/", recursive=True)]

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
    # try:
    #     PyLoader(container_builder).load(py_file)
    # except Exception as e:
    #     parse_py_file(py_file)


# for f in folders:
#     yaml_files = [f for f in glob(f'{f}/**/[!_]*.yaml', recursive=True)]
#     for yaml in yaml_files:
#         try:
#             YamlLoader(container_builder).load(yaml)
#         except Exception as e:
#             pass

#     py_files = [f for f in glob(f'{f}/**/[!_]*.py', recursive=True)]
#     for py_file in py_files[0:1]:
#         try:
#             # print(f"Loading {py_file}")
#             PyLoader(container_builder).load(py_file)
#         except Exception as e:
#             parse_py_file(py_file)


for key in list(container_builder._service_mapping.keys()):
    print(key)

