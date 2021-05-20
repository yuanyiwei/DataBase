package model

import (
	"github.com/spf13/viper"
	"log"
)

func Init() {
	viper.SetConfigName("conf")
	viper.SetConfigType("yaml")
	viper.AddConfigPath(".")

	if err := viper.ReadInConfig(); err != nil {
		log.Println(err)
	}
	log.Println("Configuration file loaded")

	var confItems = map[string][]string{
		"sql": {"user", "password", "protocol", "host", "port", "db_name"},
		"app": {"port", "cors"},
	}

	for k, v := range confItems {
		checkConfIsSet(k, v)
	}

	log.Println("All required values in configuration file are set")
}

func checkConfIsSet(name string, keys []string) {
	for i := range keys {
		wholeKey := name + "." + keys[i]
		if !viper.IsSet(wholeKey) {
			log.Println("The following item of your configuration file hasn't been set properly:", wholeKey)
		}
	}
}
