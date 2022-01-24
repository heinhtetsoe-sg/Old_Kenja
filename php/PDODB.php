<?php

require_once('PEAR.php');
// require_once("/usr/local/lib/php/for_php7.php");

/**
 * The code returned by many methods upon success
 */
define('DB_OK', 1);

/**
 * Unkown error
 */
define('DB_ERROR', -1);

/**
 * PDOException既発生個所
 */
define('PDO_IGNORE_ERROR', array(
    '22018',//getOne()で確認、既存エラー
));


class PDODB extends PEAR
{
    function connect($dsn, $usr, $pass)
    {
        $obj = new PDOCHILD;
        $obj->connect($dsn, $usr, $pass);
        return $obj;
    }
}

class STMT
{
    // fetchRowメソッド用
    public $stmt;
    // numRowsメソッド用
    public $stmt_numRows;

    public $nrows = null;

    // 結果セット無しのフェッチ実行にてエラーが発生した為、フラグ導入
    public $false_flg = true;

    function select_query($connection, $query, $params = null)
    {
        if (is_null($params))
        {
            try {
                $this->stmt = $connection->query($query);
                $this->stmt_numRows = $connection->query($query);
            } catch (PDOException $e) {
                PDOALERT::AlertMsgDisprayDialog($e);
            }
        }
        else
        {
            $i = 0;
            $params = (array)$params;

            try {
                $this->stmt = $connection->prepare($query);
                $this->stmt_numRows = $connection->prepare($query);

                foreach ($params as $param) {
                    $i++;
                    $t = gettype($param);
                    switch ($t) {
                    case "boolean":
                        $this->stmt->bindValue($i, $param, PDO::PARAM_INT);
                        $this->stmt_numRows->bindValue($i, $param, PDO::PARAM_INT);
                        break;
                    case "integer":
                        $this->stmt->bindValue($i, $param, PDO::PARAM_INT);
                        $this->stmt_numRows->bindValue($i, $param, PDO::PARAM_INT);
                        break;
                    case "double":
                        $this->stmt->bindValue($i, $param, PDO::PARAM_STR);
                        $this->stmt_numRows->bindValue($i, $param, PDO::PARAM_STR);
                        break;
                    default:
                        $this->stmt->bindValue($i, $param, PDO::PARAM_STR);
                        $this->stmt_numRows->bindValue($i, $param, PDO::PARAM_STR);
                        break;
                    }
                }
                $this->stmt->execute();
                $this->stmt_numRows->execute();
            } catch (PDOException $e) {
                PDOALERT::AlertMsgDisprayDialog($e);
            }
        }
        return $this;
    }

    //DB.phpに記述されたメソッド fetchRow($fetchmode = DB_FETCHMODE_DEFAULT, $rownum = null)
    function fetchRow($fetchmode = PDO::FETCH_NUM, $rownum = null)
    {
        $row = false;
        if ($fetchmode === DB_FETCHMODE_ORDERED)
        {
            $fetchmode = PDO::FETCH_NUM;
        }
        else if ($fetchmode === DB_FETCHMODE_ASSOC)
        {
            $fetchmode = PDO::FETCH_ASSOC;
        }

        if($this->false_flg){
            if($this->stmt != null || $this->stmt != false) {
                try {
                    if(!$rownum)
                    {
                        $row = $this->stmt->fetch($fetchmode);
                    }
                    else
                    {
                        $row = $this->stmt->fetch($fetchmode, PDO::FETCH_ORI_NEXT, $rownum);
                    }
                } catch (PDOException $e) {
                    PDOALERT::AlertMsgDisprayDialog($e);
                }
            }
        }

        if($row === false)
        {
            $this->false_flg = false;
            $row = NULL;
        }

        return $row;
    }

    //元はDB_result::numRows()
    function numRows()
    {
        if (is_null($this->nrows)) {
            try {
                $allRows = $this->stmt_numRows->fetchAll();
            } catch (PDOException $e) {
                PDOALERT::AlertMsgDisprayDialog($e);
            }
            $this->nrows = ((is_array($allRows) || $allRows instanceof countable) ? count($allRows) : 0);
        }

        return $this->nrows;
    }

    //DB.phpに記述されたメソッド
    function free()
    {
        $this->stmt->closeCursor();

        return true;
    }
}

class PDOCHILD
{

    public $connection;

    public $statement;

    function connect($dsn, $user, $password)
    {
        $dsn = $this->checkDSN($dsn);
        try {
            $this->connection =  new PDO(
                $dsn,
                $user,
                $password,
                array(
                    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                    PDO::ATTR_PERSISTENT => TRUE,
                )
            );
            //エミュレーションをOFF
            $this->connection->setAttribute(PDO::ATTR_EMULATE_PREPARES, false);
        } catch (PDOException $e) {
            PDOALERT::AlertMsgDisprayDialog($e);
        }

        return DB_OK;
    }

    function disconnect()
    {
        $this->connection = null;
        return true;
    }

    //PEAR用のDSNをPDO_ibm_DSN用に成形するためのメソッド
    //prepend.incにて修正している場合は問題無し。
    //ドライバーが変更になると対応できない。
    function checkDSN($dsn){
        $parsed = array(
            'protocol' => false,
            'hostspec' => false,
            'port'     => false,
            'database' => false,
        );

        //PEAR用DSNであるかを判断
        if ((strpos($dsn, '://')) === false) {
            return $dsn;
        }
        $at = strrpos($dsn,'@');
        $dsn = substr($dsn, $at + 1);

        if (preg_match('|^([^(]+)\((.*?)\)/?(.*?)$|', $dsn, $match)) {
        } else {
            if (strpos($dsn, '+') !== false) {
                list($proto, $dsn) = explode('+', $dsn, 2);
            }
            if (strpos($dsn, '/') !== false) {
                list($proto_opts, $dsn) = explode('/', $dsn, 2);
            } else {
                $proto_opts = $dsn;
                $dsn = null;
            }
        }

        $parsed['protocol'] = (!empty($proto)) ? $proto : 'tcp';
        if (strpos($proto_opts, ':') !== false) {
            list($proto_opts, $parsed['port']) = explode(':', $proto_opts);
        }
        if ($parsed['protocol'] == 'tcp') {
            $parsed['hostspec'] = $proto_opts;
        }

        if ($dsn) {
            if (($pos = strpos($dsn, '?')) === false) {
                $parsed['database'] = $dsn;
            } 
        }

        //DSNの成形
        //databaseのみなのか、host:portありなのか判定する。
        if(!empty($parsed['database']) && !empty($parsed['port']) && !empty($parsed['hostspec'])){
            $replaceDSN = "ibm:DRIVER={IBM DB2 ODBC DRIVER};DATABASE=".$parsed['database'].";HOSTNAME=".$parsed['hostspec'].";PORT=".$parsed['port'].";PROTOCOL=TCPIP;";
            return $replaceDSN;

        }else{
            //DBNAMEオンリーの場合は、カタログしたDBに接続する。
            $replaceDSN = "ibm:".$parsed['hostspec'];
            return $replaceDSN;
        }
    }

    //function query($query, $params = array())
    function query($query, $params = null)
    {
        preg_match('/insert|delete|update|select|with/i',$query,$result);

        //select句 or with句
        if (strtolower($result[0]) == 'select' or strtolower($result[0]) == 'with')
        {
            try {
                $classstmt = new STMT;
                $select_stmt = $classstmt->select_query($this->connection, $query, $params);
            } catch (PDOException $e) {
                PDOALERT::AlertMsgDisprayDialog($e);
            }

            return $select_stmt;
        }
        //insert句 or update句 or delete句
        else
        {
            try {
                if (is_null($params))
                {
                    $this->statement = $this->connection->query($query);
                    return DB_OK;
                }
                else
                {
                    $this->statement = $this->preparedStmtExcute($query, $params);
                    return DB_OK;
                }
            } catch (PDOException $e) {
                if($this->connection->inTransaction){
                    $this->connection->rollback();
                }
                PDOALERT::AlertMsgDisprayDialog($e);
            }
        }
    }


    //DB/common.phpに記述されたメソッド
    //FETCHMODEの分岐実装不要なメソッド。
    function getOne($query, $params = null)
    {

        try {
            if (is_null($params))
            {
                $this->statement = $this->connection->query($query);
                $row = $this->statement->fetch(PDO::FETCH_NUM);
            }
            else
            {
                $this->statement = $this->preparedStmtExcute($query, $params);
                $row = $this->statement->fetch(PDO::FETCH_NUM);
            }
        } catch (PDOException $e) {
            PDOALERT::AlertMsgDisprayDialog($e);
        }
        return $row[0];
    }


    //PEARのフェッチモードである、DB_FETCHMODE_OBJECTは使われていない。(DB_FETCHMODE_DEFAULTを渡している箇所もなし)
    //DB_FETCHMODE_ORDEREDはPDO::FETCH_NUM, BE_FETCHMODE_ASSOCはPDO::FETCH_ASSSOCで実装します。
    //DB/common.phpに記述されたメソッド
    function &getRow($query, $params = null,
                        $fetchmode = PDO::FETCH_NUM)
    {
        //第二引数$paramsがwhere句に渡すための配列（パラメータ）でない場合、$fetchmodeに$paramsを格納する。
        if (!is_null($params)) 
        {
            if($params == DB_FETCHMODE_ASSOC || $params == DB_FETCHMODE_ORDERED || $params == DB_FETCHMODE_OBJECT) 
            {
                $fetchmode = $params;
                $params = null;
            }
            else
            {
                $params = (array)$params;
            }
        }
        
        if ($fetchmode === DB_FETCHMODE_ORDERED)
        {
            $fetchmode = PDO::FETCH_NUM;
        }
        else if ($fetchmode === DB_FETCHMODE_ASSOC)
        {
            $fetchmode = PDO::FETCH_ASSOC;
        }

        try {
            if (is_null($params))
            {
                $this->statement = $this->connection->query($query);
                $row = $this->statement->fetch($fetchmode);
            }
            else
            {
                $this->statement = $this->preparedStmtExcute($query, $params);
                $row = $this->statement->fetch($fetchmode);
            }
        } catch (PDOException $e) {
            PDOALERT::AlertMsgDisprayDialog($e);
        }

        if($row === false)
        {
            $row = NULL;
        }

        return $row;
    }



    //DB/common.phpに記述されたメソッド
    function &getAll($query, $params = null,
                     $fetchmode = PDO::FETCH_NUM)
    {
        //第二引数$paramsがwhere句に渡すための配列（パラメータ）でない場合、$fetchmodeに$paramsを格納する。
        if (!is_null($params)) 
        {
            if($params == DB_FETCHMODE_ASSOC || $params == DB_FETCHMODE_ORDERED || $params == DB_FETCHMODE_OBJECT) 
            {
                $fetchmode = $params;
                $params = null;
            }
            else
            {
                $params = (array)$params;
            }
        }
        
        if ($fetchmode === DB_FETCHMODE_ORDERED)
        {
            $fetchmode = PDO::FETCH_NUM;
        }
        else if ($fetchmode === DB_FETCHMODE_ASSOC)
        {
            $fetchmode = PDO::FETCH_ASSOC;            
        }

        try {
            if (is_null($params))
            {
                $this->statement = $this->connection->query($query);
                    $results = $this->statement->fetchAll($fetchmode);
            }
            else
            {
                $this->statement = $this->preparedStmtExcute($query, $params);
                $results = $this->statement->fetchAll($fetchmode);            
            }
        } catch (PDOException $e) {
            PDOALERT::AlertMsgDisprayDialog($e);
        }

        return $results;
    }

    function &getAssoc($query, $force_array = false, $params = null,
                       $fetchmode = PDO::FETCH_NUM, $group = false)
    {

        //フェッチモードをPEAR->PDOに変換する
        //現状、DB_FETCHMODE_OBJECT、DB_FETCHMODE_ASSOCは未使用
        //DB_FETCHMODE_DEFAULTもFW内でしか利用ないので不要。
        if ($fetchmode === DB_FETCHMODE_OBJECT)
        {
            $fetchmode = PDO::FETCH_OBJ;
        }
        else if ($fetchmode === DB_FETCHMODE_ASSOC)
        {
            $fetchmode = PDO::FETCH_ASSOC;
        }
        else if ($fetchmode === DB_FETCHMODE_ORDERED || $fetchmode = DB_FETCHMODE_DEFAULT)
        {
            $fetchmode = PDO::FETCH_NUM;
        }
        
        //クエリ実行
        try {
            if (is_null($params))
            {
                $this->statement = $this->connection->query($query);
            }
            else
            {
                $this->statement = $this->preparedStmtExcute($query, $params);
            }
        } catch (PDOException $e) {
            PDOALERT::AlertMsgDisprayDialog($e);
        }

        $cols = $this->statement->columnCount();

        $results = array();

        if ($cols > 2 || $force_array) {
            if ($fetchmode == PDO::FETCH_ASSOC) {
                while (is_array($row = $this->statement->fetch(PDO::FETCH_ASSOC))) {
                    reset($row);
                    $key = current($row);
                    unset($row[key($row)]);
                    if ($group) {
                        $results[$key][] = $row;
                    } else {
                        $results[$key] = $row;
                    }
                }
            } elseif ($fetchmode == PDO::FETCH_OBJ) {
                while ($row = $this->statement->fetch(PDO::FETCH_OBJ)) {
                    $arr = get_object_vars($row);
                    $key = current($arr);
                    if ($group) {
                        $results[$key][] = $row;
                    } else {
                        $results[$key] = $row;
                    }
                }
            } else {
                while (is_array($row = $this->statement->fetch(PDO::FETCH_NUM))) {
                    // we shift away the first element to get
                    // indices running from 0 again
                    $key = array_shift($row);
                    if ($group) {
                        $results[$key][] = $row;
                    } else {
                        $results[$key] = $row;
                    }
                }
            }
        } else {
            // return scalar values
            while (is_array($row = $this->statement->fetch(PDO::FETCH_NUM))) {
                if ($group) {
                    $results[$row[0]][] = $row[1];
                } else {
                    $results[$row[0]] = $row[1];
                }
            }
        }

        return $results;
    }


    function &getCol($query, $col = 0, $params = null)
    {
        try {
            if (is_null($params))
            {
                $this->statement = $this->connection->query($query);
            }
            else
            {
                $this->statement = $this->preparedStmtExcute($query, $params);
            }

            $results = array();
            if(is_int($col))
            {
                while (is_array($row = $this->statement->fetch(PDO::FETCH_NUM))) {
                    $results[] = $row[$col];
                }
            }
            else
            {
                while (is_array($row = $this->statement->fetch(PDO::FETCH_ASSOC))) {
                    $results[] = $row[$col];
                }
            }

        } catch (PDOException $e) {
            PDOALERT::AlertMsgDisprayDialog($e);
        }

        return $results;
    }

    function affectedRows()
    {
        if(is_null($this->statement)){
            return 0;
        }elseif(is_object($this->statement)){
            return $this->statement->rowCount();
        }else{
            return $this->statement;
        }
    }

    //PDO環境では不要となっているが、PEAR環境では必要となるため残している。
    //src側で利用する個所がなくなる場合は廃棄。
    function expectError($code = '*')
    {
        // $this->pearIns = new PEAR();
        // $count = $this->pearIns->expectError($code);
        // return $count;
    }

    function popExpect()
    {
        // $err = $this->pearIns->popExpect();
        // return $err;
    }

    //DB/odbc.phpに記述されたメソッド
    //賢者の一部では、autoCommit(false)を実行した後、autoCommit(true)を実行している箇所あり。
    //PEARのautoCommit(true)を行った場合、それまでのトランザクション内容がある場合commitされる。
    function autoCommit($onoff = false)
    {
        if($onoff === false)
        {
            try {
                $this->connection->beginTransaction();
            } catch (PDOException $e) {
                PDOALERT::AlertMsgDisprayDialog($e);
            }
        }
        else
        {
            if($this->connection->inTransaction()){
                $this->connection->commit();
            }
        }
        return DB_OK;
    }
    
    function commit()
    {
        if($this->connection->inTransaction()){
            try {
                $this->connection->commit();
            } catch (PDOException $e) {
                $this->connection->rollback();
                PDOALERT::AlertMsgDisprayDialog($e);
            }
        }
        return DB_OK;
    }

    function rollback()
    {
        if($this->connection->inTransaction()){
            $this->connection->rollback();
        }
        return DB_OK;
    }


    //DB.phpに記述されたメソッド
    function free()
    {
        $this->statement->closeCursor();
        return true;
    }

    //プリペアドステートメント実行メソッド
    //将来実装環境、SQLインジェクション対策
    function preparedStmtExcute($query, $params)
    {
        $i = 0;
        $params = (array)$params;

        try {
            $results = $this->connection->prepare($query);

            foreach($params as $param)
            {
                $i++;
                $t = gettype($param);
                switch($t){
                    case "boolean":
                        $results->bindValue($i, $param, PDO::PARAM_INT);
                        break;
                    case "integer":
                        $results->bindValue($i, $param, PDO::PARAM_INT);
                        break;
                    case "double":
                        $results->bindValue($i, $param, PDO::PARAM_STR);
                        break;
                    default:
                        $results->bindValue($i, $param, PDO::PARAM_STR);
                        break;
                }
            }
            $results->execute();
        } catch (PDOException $e) {
            PDOALERT::AlertMsgDisprayDialog($e);
        }      
            return $results;
    }

}
//PDOExceptionをログ出力、ダイアログ表示用クラス
//現状errorスルー個所のダイアログ非表示対応。想定エラーコードのみ定数定義してます。
//個別queryメソッドでスルー対応は管理できなくなるので一括で対応。
class PDOALERT
{

    static function AlertMsgDisprayDialog($e){
        // akimoto:現在エラーログ使っていない。
        // 下記暫定ファイルのパス。パーミッションの操作必要なのでchmod()追加必要か？
        if (in_array($e->getCode(), PDO_IGNORE_ERROR, true)) {
            error_log("[".date('Y-m-d H:i:s')."]IGNORE\n".$e."\n", 3, "/tmp/errorlog.log");
            return;
        }
        $errorinfo = "エラーが発生しました。\\nエラーコードをお控えの上、ヘルプデスクまでご連絡ください";
        $errorinfo .= "\\nエラー内容：\\n".str_replace(array("\"", "\r\n", "\r", "\n"), "", $e->getMessage());
        $errorinfo .= "\\nエラーコード：".$e->getCode();
        $errorinfo .= "\\n対象コード：".$e->getFile().":".$e->getLine()."行目";
        $errorinfo .= "\\nStack trace:";
        foreach ($e->getTrace() as $key => $value) {
            $errorinfo .= "\\n[".$key."]:".$value['file']."(".$value['line'].") :".$value['class'].$value['type'].$value['function']."()";
        }
        echo '<script>alert("'.$errorinfo.'");</script>';
        error_log("[".date('Y-m-d H:i:s')."]ERROR\n".$e."\n", 3, "/tmp/errorlog.log");
        return;
    }
}