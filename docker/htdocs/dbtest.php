<?php

//DB_TEST
define("DB_DATABASE", "CHUKOUDB");
define("DB_HOST", "10.250.250.208");
define("DB_PORT", "50000");

define("USER", "db2inst1");
define("PASSWORD", "db2kenja");

$_ = function($s){return $s;};
$dsn = "ibm:DRIVER={IBM DB2 ODBC DRIVER};DATABASE={$_(DB_DATABASE)};" . "HOSTNAME={$_(DB_HOST)};PORT={$_(DB_PORT)};PROTOCOL=TCPIP;";
define("DSN" , $dsn);


try {
    $db =  new PDO(DSN, USER, PASSWORD);
} catch (PDOException $e) {
    echo "DB2 connect error";
    exit();
}

$result = $db->query("SELECT SYSDATE FROM SYSIBM.SYSDUMMY1");
$fetch = $result->fetch(PDO::FETCH_ASSOC);

var_dump($fetch);


?>