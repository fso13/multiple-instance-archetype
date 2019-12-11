##Multiple Instance Project.
В файле конфигурации `osgi.service.InstanceName` является обязательным параметром.

---
###1.  Add feature repositories.
Run command in Karaf:
```sh
repo-add mvn:***/***/***/xml/features
```    
###2.  Install project feature.
Run command in Karaf:
```sh   
feature:install ***
```
###3.  Apply development configuration.
Example config file *** for development:
```properties
osgi.service.InstanceName=name1
```
