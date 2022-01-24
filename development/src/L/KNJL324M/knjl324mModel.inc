<?php

require_once('for_php7.php');

class knjl324mModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjc171aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("DATE"         =>  VARS::post("DATE"),         //作成日付
                             "YUKO_DATE"    =>  VARS::post("YUKO_DATE"),    //有効期間
                             "LIMIT_DATE"   =>  VARS::post("LIMIT_DATE"),   //入学手続き期間
                             "LIMIT_TIME"   =>  VARS::post("LIMIT_TIME"),   //時間
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                             );

        $this->test_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");
    }

    /**************/
    /* ＣＳＶ出力 */
    /**************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        //ヘッダー
        $contents  = "";    //クリア
        $heisei_year = common::DateConv1($this->test_year.'/05/01', 10);//和暦になる(例：平成 9年度)(月、日は適当です。)
        $contents .= "{$heisei_year}　合格者\n";
        $date       = common::DateConv1(str_replace("-", "/", $this->field["DATE"]), 3);//和暦になる(例：平成 9年 9月17日（月）)
        $yuko_date  = common::DateConv1(str_replace("-", "/", $this->field["YUKO_DATE"]), 3);//和暦になる(例：平成 9年 9月17日（月）)
        $limit_date = common::DateConv1(str_replace("-", "/", $this->field["LIMIT_DATE"]), 3);//和暦になる(例：平成 9年 9月17日（月）)
        $query = knjl324mQuery::selectMain_Cout_Query($this);
        $count_goukakusya = $db->getOne($query);
        $contents .= "作成日付 {$date},,,,合格者数 {$count_goukakusya}名\n";

        //SQL文発行
        $examnoArray = array();
        $query = knjl324mQuery::selectMainQuery($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $examnoArray[] = ($row["EXAMNO"]) ? (int)$row["EXAMNO"] : "";
            if (get_count($examnoArray) == 10) {
                $contents .= implode(",", $examnoArray);
                $contents .= "\n";
                $examnoArray = array();
            }
            $csv_flag = true;
        }
        if (get_count($examnoArray) > 0) {
            $contents .= implode(",", $examnoArray);
        }

        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($csv_flag) {
            if($this->field["OUTPUT"]=="3"){        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "合格者発表資料データ";
            }
            $filename = sprintf("%s.csv",$couse);
            $filename = i18n_convert($filename,"SJIS-win");
            $contents = i18n_convert($contents,"SJIS-win");
            /* HTTPヘッダの出力 */
            i18n_http_output("pass");
            header("Accept-Ranges: none");
            header("Content-Disposition: inline; filename=$filename");
            header("Content-Transfer-Encoding: binary");
            header("Content-Length: ". strlen($contents) );
            header("Content-Type: text/octet-stream");

            echo $contents;
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }


    function clean() {}
}
?>
