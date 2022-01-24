<?php

require_once('for_php7.php');
require_once('PDODB.php');

// 2006-06-15 tamura: 'DATE' 対応
// 2006-06-21 tamura: date2Str() を date2sql() に変更
// 2006-08-03 o-naka: 'DATE' 対応の不具合を修正

define("TEXT",      0);
define("NUMBER" ,   1);
define("FUNC",      2);
define("DATE",      3);
define("PREPARE",   4);

class Query extends PDODB {

    function &dbCheckOut()
    {
        return PDODB::connect(DSN, DB_USER, DB_PASSWORD);
    }

    function &dbCheckOut2()
    {
        return PDODB::connect(DSN2, DB_USER2, DB_PASSWORD2);
    }

    function dbCheckIn(&$con)
    {
        $con->disconnect();
    }
    //'->''に変換
    function addquote($str)
    {
        return str_replace("'", "''", $str);
    }

    function date2sql($date) {
        return str_replace("/", "-", $date);
    }

    //insert文作成
    function insertSQL($arg, $table)
    {
        $fields = array_keys($arg);
        $params = array();
        $param_i = 0;

        $sql = "insert into $table(" .implode($fields, ",") .") ";
        $sql .= "values(";
        $sp = "";
        foreach($fields as $f){
            $key = key($arg[$f]);
            if (trim($arg[$f][$key]) == ''){
                $sql .= $sp ."NULL";
            }else{
                switch($key){
                    case TEXT:
                        $sql .= $sp ."'" .Query::addquote($arg[$f][$key]) ."'";
                        break;
                    case NUMBER:
                        $sql .= $sp .$arg[$f][$key];
                        break;
                    case FUNC:
                        $sql .= $sp .$arg[$f][$key];
                        break;
                    case DATE:
                        $sql .= $sp ."'" .Query::date2sql($arg[$f][$key]) ."'";
                        break;
                    case PREPARE:
                        $sql .= $sp ."?";
                        $params[$param_i] = $arg[$f][$key];
                        $param_i++;
                        break;
                    default;
                        $sql .= $sp .$arg[$f][$key];
                        break;
                }
            }
            $sp = ",";
        }
        if(!$params)
        {
            return $sql .= ")";
        }else
        {
            $results = array();
            $results[0] = $sql .= ")";
            $results[1] = $params;
            return $results;

        }
    }
    //update文作成
    function updateSQL($arg, $table, $where)
    {
        $fields = array_keys($arg);
        $params = array();
        $param_i = 0;

        $sql = "UPDATE $table SET ";
        $sp = "";
        foreach($fields as $f){
            $key = key($arg[$f]);
            if (trim($arg[$f][$key]) == ''){
                $sql .= $sp ."$f = NULL";
            }else{
                switch($key){
                    case TEXT:
                        $sql .= $sp ."$f = '" .Query::addquote($arg[$f][$key]) ."'";
                        break;
                    case NUMBER:
                        $sql .= $sp ."$f = " .$arg[$f][$key];
                        break;
                    case FUNC:
                        $sql .= $sp ."$f = " .$arg[$f][$key];
                        break;
                    case DATE:
                        $sql .= $sp ."$f = '" .Query::date2sql($arg[$f][$key]) ."'";
                        break;
                    //プリペアドステートメント処理
                    case PREPARE:
                        $sql .= $sp ."$f = ?";
                        $params[$param_i] = $arg[$f][$key];
                        $param_i++;
                        break;
                    default;
                        $sql .= $sp ."$f = " .$arg[$f][$key];
                        break;
                }
            }
            $sp = ",";
        }
        if(!$params)
        {
            return $sql . " " . $where;
        }else
        {
            $results = array();
            $results[0] = $sql . " " . $where;
            $results[1] = $params;
            return $results;

        }
    }
    function &getCacheDB()
    {
        $cache = new Cache_DB("file",
                               array("cache_dir"       => CACHEDIRECTORY."/",
                                     "filename_prefix" => "query_"),
                                     CACHEEXPIRES);

        $flg = preg_match("/(true|yes|on)/i",CACHING) ? true : false;
        $cache->setCaching($flg);

        $cache->setConnection(DSN);
        return $cache;
    }

    /**
     *   データーベースのキャシュをクリアします。
     */
    function removeCacheDB($query)
    {
        if ( Cache::isError($cache = Query::getCacheDB()) ) {
            return $cache;
        }
        //$cache->flush('db_cache');
        $cache->remove(md5($query),'db_cache');

    }

    function &call_method($class, $method, $options = null)
    {
        $_class = new $class();
        $result = call_user_func_array(array($_class,$method), $options);
        unset($_class);
        return $result;
    }
}
?>
