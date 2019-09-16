#!/bin/bash

UserPort=$1
DefaultPort=8090
CurrentDir="$(pwd)"

echo current directory :: $CurrentDir
echo user port :: $UserPort
echo default port :: $DefaultPort

IsRunning="$(ps -ef | grep TerraSpin.jar | wc -l)"	 

if [ $IsRunning -gt 1 ]
then
    echo TerraSpin service is already running.
else

		wget -O TerraSpin.jar "https://00e9e64bacad65f5ac8b282ebf5e841251829b82d3d60552a2-apidata.googleusercontent.com/download/storage/v1/b/terraspin-binary/o/TerraSpin.jar?qk=AD5uMEtg1Iky-a7oH5T28-x237kgCaBBTeioC4S-n4eNwt0mmbTcogcJd1BVxISqkzpKwqUFlNSYiWLhO5ZIyy9EFO2haNFJE4ZJKB9hhBnrG3TZLgTfVT4PZFku9OolpAUg-sG0M9CrOo1EYyyIV8_x7c5M_tff1Fa4HDus3NzPVf8gVW_7x2TkMICbbXHFvZcmv1JvjgNu-A06AxbjWzR3zm3ME49_D3jGH4dXW-j8tXx-ss6H6fjTNCL93YakWVgA9kcTkImU85AaJh64T3wQFLjGoMt2fbbtOQEDhHBeT8aGSObQu9hwCJkg_SCmtkA3ZKDc8_YBtNaVWbwtywM5SzqUzvrkGFmGpMfZ3sPYwDYEbTtpn6E9QpRaxrIlY6BziYirlMI6TJjLxGn_aeBzfR7M9lCOBW0Rw4936jDTW6UHhXY-W73e9Gw0FCq_t4LI_onKYKlAvrFay22787NjhIERlz7xwHY1T902ZRq3Evo4qxX4eSGeyNMft_KVZM3lm1Uj0mdP9-2jRwf7jZERBK0RN7B6imkz1m9XKNWAlsyGK56p3iyY4Tsal66gnP_8I5GT-I04XlPRcolR8MeqIxiYezrebPErqfBybcIvYpXLpxD9JoQ1Uzh-sxpECUo5PR-gb6DEFeqRjXlRjt_U04DfJm3GpFv6a6h992tEyDsT9SaIZ9IbRMeCtU2TUC9zdLUrzgNnREJUjBijC-y-4jca74uLwR-AfSg-m--UszNd766dbof9kgsblTfCOJ58a5iMc_TOLSQ6u2vGq0ID-Z_gQhzDmkfgB_La45k1ZflZO67YrLI"

		if [ -z "$UserPort" ]
    then
        nohup java -Dserver.port=8090 -jar $CurrentDir/TerraSpin.jar 2>&1 &
    else
        nohup java -Dserver.port=$UserPort -jar $CurrentDir/TerraSpin.jar 2>&1 &
    fi
fi
