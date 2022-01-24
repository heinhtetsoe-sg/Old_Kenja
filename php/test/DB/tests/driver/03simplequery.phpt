--TEST--
DB_driver::simpleQuery
--INI--
error_reporting = 2047
--SKIPIF--
<?php

require_once('for_php7.php');
 chdir(dirname(__FILE__)); require_once './skipif.inc'; ?>
--FILE--
<?php

require_once('for_php7.php');

require_once './mktable.inc';
require_once '../simplequery.inc';
?>
--EXPECT--
passed
