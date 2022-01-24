<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl324Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl324Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("APDIV"            =>  VARS::post("APDIV"),            //入試制度
                             "TESTDV"           =>  VARS::post("TESTDV")            //入試区分
                     );

        $this->cmd         = VARS::request("cmd");
    }
    /**
     * ＣＳＶ出力
     */
    function getDownloadModel()
    {
            //DB接続
            $db = Query::dbCheckOut();

            //SQL文発行（入試区分・入試区分名称・入試日付を取得）
            $result = $db->query(knjl324Query::getNameMst($this));
            $testname = $testdate = array();
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row = str_replace(",",".",$row);//データ内にカンマがあればカンマに置換・・・置換する文字は、カンマでなければ何でも良い。
                //入試区分名称
                $testname[$row["NAMECD2"]] = $row["NAME1"];
                //入試日付
                $testdate[$row["NAMECD2"]] = ($row["NAMESPARE1"]!="") ? substr(common::DateConv1(str_replace("-","/",$row["NAMESPARE1"]),0), 12) : "";
            }

            //SQL文発行（アップ・スライド合格を取得）
            $result = $db->query(knjl324Query::getNameMst2());
            $judgeClassName = array();
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $judgeClassName[$row["NAMECD2"]] = $row["NAME1"];
            }

            //SQL文発行（合格者取得）
            $result = $db->query(knjl324Query::getNo($this));
            //ヘッダ
            $title = "入学試験合格者受験番号一覧";
            $nendo = $this->ObjYear."/04/01";
            $nendo = common::DateConv1($nendo,10);                              //年度
            $ctrl_date = common::DateConv1(str_replace("-","/",CTRL_DATE),0);   //作成日
            //変数の初期値
            $contents   = "";       //CSV出力フィールド
            $data       = false;    //CSV出力フラグ
            $recordcnt  = 1;        //列数
            $testdiv    = "9";      //入試区分保管用
            $div        = "0";      //区分保管用
            for ($i=1; $i<7; $i++) $csvdata[$i] = "";   // Add 05/01/10
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row = str_replace(",",".",$row);//データ内にカンマがあればカンマに置換・・・置換する文字は、カンマでなければ何でも良い。
                //入試区分のブレイク時
                if ($testdiv != $row["TESTDIV"] || $div != $row["DIV"]) {
                    //改行・列数の初期化
                    if ($testdiv != "9") {
                        $contents .= implode(",", $csvdata);        // Add 05/01/10
                        for ($i=1; $i<7; $i++) $csvdata[$i] = "";   // Add 05/01/10
                        $contents .= "\r\n";
                        $recordcnt = 1;
                    }
                    //ヘッダ
                    if ($testdiv != $row["TESTDIV"]) {
                        $contents .= $nendo."　".$testname[$row["TESTDIV"]]."　".$title."\r\n";
                        $contents .= "入試日 ： ".$testdate[$row["TESTDIV"]]."\r\n";
                        $contents .= "作成日 ： ".$ctrl_date."\r\n";
                        $contents .= "通常合格\r\n";
                    } else {
                        $contents .= $judgeClassName[$row["JUDGECLASS"]]."\r\n";
                    }
                }
                //列数が６列目以上の時、改行・列数の初期化
                if ($recordcnt > 6) {
                    $contents .= implode(",", $csvdata);        // Add 05/01/10
                    for ($i=1; $i<7; $i++) $csvdata[$i] = "";   // Add 05/01/10
                    $contents .= "\r\n";
                    $recordcnt = 1;
                }
                $testdiv = $row["TESTDIV"];         //入試区分の保管
                $div = $row["DIV"];         //区分の保管
                //$contents .= $row["EXAMNO"].",";  //受験番号
                $markHonordiv = ($row["DIV"] == "0" && $row["HONORDIV"] == "1") ? "☆" : "";
                $mark = ($row["DIV"] == "0" && $row["JUDGECLASS"] == "3") ? "◎" : "";
                //1,4,6合格者に対して、特別アップ合格者でもある場合に”◎”のマークをつける
                if ($row["JUDGECLASS"] == "1" || $row["JUDGECLASS"] == "4" || $row["JUDGECLASS"] == "6") {
                    $cnt = $db->getOne(knjl324Query::getSpUpJudge($this, $row));
                    if (0 < $cnt) $mark = "◎";
                }
                $csvdata[$recordcnt] = $markHonordiv.$mark.$row["EXAMNO"];    //受験番号 Add 05/01/10
                $recordcnt++;                       //列数カウント

                $data = true;
            }
            $contents .= implode(",", $csvdata);        // Add 05/01/10
            $contents .= "\r\n";
            $contents .= ",,,,,\r\n";   //Add 05/01/18
            $result->free();
            Query::dbCheckIn($db);

            //対象データ無し
            if (!$data){
                $this->setWarning("MSG303");
                return false;
            }

        //$couse="合格者受験番号";
        $couse="goukakunumber";
        $filename = sprintf("%s.csv",$couse);
        common::downloadContents($contents, $filename);
        return true;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
