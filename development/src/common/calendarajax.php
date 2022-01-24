<?php

require_once('for_php7.php');

// 2019/01/16 大城作成

$wareki = new calendarajax();
$wareki->compatible();
$wareki->main($_REQUEST);

class calendarajax{
    // 和暦のリスト(getWarekiList返却値)
    var $warekiList = array();

    //コンストラクタ
    function calendarajax(){
    }

    function main($request){
       while ( true ) {
            switch (trim($request['cmd'])) {
                case "getWarekiList":
                    $response = $this->getWarekiList();
                    echo json_encode($response);
                    break 2;
            }
        }
    }

    function compatible() {
        if(!function_exists("json_encode")) {
            function json_encode($param) {
                require_once("JSON.php");
                $json = new Services_JSON();
                return $json->encode($param);
            }
            function json_decode($param, $assoc = false) {
                require_once("JSON.php");
                $json = new Services_JSON($assoc ? SERVICES_JSON_LOOSE_TYPE : 0);
                return $json->decode($param);
            }
        }
    }

    /*
        @(f)

        機能      : 和暦(元号)のリストを取得する

        引き数    : なし

        返り値    : 和暦のリスト

        機能説明  : 名称マスタ(NAME_MST)より和暦を取得し返却する
                    取得条件：NAMECD1='L007'
        備考      :
          和暦のリスト
           list[0]['CD']    = '1'
           list[0]['Name']  = '明治'
           list[0]['SName'] = 'M'
           list[0]['Start'] = '1868/09/09'
           list[0]['End']   = '1912/07/29'
             :
           list[3]['CD']    = '4'
           list[3]['Name']  = '平成'
           list[3]['SName'] = 'H'
           list[3]['Start'] = '1989/01/08'
           list[3]['End']   = '2019/04/30'
             :
           list[n]['CD']    = '9'
           list[n]['Name']  = '〇〇'  ←新元号
           list[n]['SName'] = 'X'
           list[n]['Start'] = 'XXXX/XX/XX'
           list[n]['End']   = '9999/XX/XX'
    */
    function getWarekiList(){

        // 既に取得している場合は、DBから再取得しない
        if (get_count($this->warekiList) <= 0) {
            $db = Query::dbCheckOut();
            $query .= "SELECT ";
            $query .= "  NAMECD1 ";
            $query .= "  , NAMECD2 ";
            $query .= "  , NAME1 ";
            $query .= "  , ABBV1 ";
            $query .= "  , ABBV3 ";
            $query .= "  , NAMESPARE1 ";
            $query .= "  , NAMESPARE2 ";
            $query .= "  , NAMESPARE3 ";
            $query .= "FROM DB2INST1.NAME_MST ";
            $query .= "WHERE NAMECD1 =  'L007' ";
            $query .= "ORDER BY NAMECD2 ";

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->warekiList[] = array(
                  "CD" => $row['NAMECD2'],
                  "Name" => $row['NAME1'],
                  "SName" => $row['ABBV1'],
                  "Start" => $row['NAMESPARE2'],
                  "End" => $row['NAMESPARE3'],
                  "YearStart" => $row['NAMESPARE1'],
                  "YearEnd" => $row['ABBV3'],
                );
            }
            $result->free();
            Query::dbCheckIn($db);
        }
        return $this->warekiList;
    }
}
?>
