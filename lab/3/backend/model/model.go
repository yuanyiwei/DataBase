package model

import (
	"fmt"
	"github.com/spf13/viper"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"log"
)

var mydb *gorm.DB

func getDatabaseLoginInfo() string {
	loginInfo := viper.GetStringMapString("sql")
	return fmt.Sprintf("%s:%s@%s(%s:%s)/%s?tls=skip-verify&parseTime=true&loc=Asia%%2FShanghai",
		loginInfo["user"],
		loginInfo["password"],
		loginInfo["protocol"],
		loginInfo["host"],
		loginInfo["port"],
		loginInfo["db_name"])
}

func Connect() {
	dsn := getDatabaseLoginInfo()
	log.Println("Connecting MySQL")
	var err error
	mydb, err = gorm.Open(mysql.Open(dsn), &gorm.Config{})
	if err != nil {
		log.Println(err)
	}
	if mydb == nil {
		log.Println("DB is nil")
	}
	log.Println("MySQL connected")
}
