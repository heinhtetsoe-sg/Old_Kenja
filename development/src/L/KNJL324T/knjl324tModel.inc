<?php

require_once('for_php7.php');

class knjl324tModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjc171aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("DATE"         =>  VARS::post("DATE"),         //対象日
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
        $nendoDate = $this->test_year ."/04/01";
        $nendo = common::DateConv1($nendoDate, 10);//和暦になる(例：平成 9年度)
        $contents .= ",{$nendo}　合格者\n";
        $date = common::DateConv1(str_replace("-", "/", $this->field["DATE"]), 3);//和暦になる(例：平成 9年 9月17日（月）)
        $contents .= ",,,,{$date}\n";

        $query = knjl324tQuery::getSchoolName($this);
        $schoo_name = $db->getOne($query);
        $csv_flag = false;   //出力フラグ
        $contents .= ",,,,{$schoo_name}\n";

        //SQL文発行
        $cell_count = 1; //五列で改行する為のカウンタ
        $examnoArray = array();
        $query = knjl324tQuery::selectMainQuery($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            if ($row["SUC_MAJORCD"] != $before_suc_majorcd) { //学科コードが変わったら
                if (get_count($examnoArray) > 0) {
                    $contents .= implode(",", $examnoArray);
                    $contents .= "\n";
                    $examnoArray = array();
                }
                $contents .= ("0" == substr($row["SUC_MAJORCD"], -1)) ? $row["MAJORLNAME"] : $row["MAJORSNAME"];
                $contents .= "\n";
            }
            $examnoArray[] = (int) $row["EXAMNO"];
            if (get_count($examnoArray) == 5) {
                $contents .= implode(",", $examnoArray);
                $contents .= "\n";
                $examnoArray = array();
                $cell_count = 1;
            }
            $before_suc_majorcd = $row["SUC_MAJORCD"];
            $cell_count++;
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
                $couse = "合格者一覧";
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
