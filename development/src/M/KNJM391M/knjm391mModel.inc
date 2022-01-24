<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm391mModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjm391mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "DATE"         =>  VARS::post("DATE"),         //処理年月日
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }


    //CSV出力処理
    function getDownloadModel() {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        //DB接続
        $db = Query::dbCheckOut();

        //CSVデータを取得し出力変数にセット
        $data = false;
        $dataContents1 = "";
        $dataContents2 = "";
        $baseYear = $db->getOne("VALUES(FISCALYEAR(DATE('".str_replace('/', '-', $this->field["DATE"])."')))");

        //ヘッダ配列
        $header = array("",
                        "出校人数計",
                        "一般生",
                        "編入生",
                        "",
                        "ＨＲ行事計",
                        "ＨＲ",
                        "行事",
                        "",
                        "卒予ＨＲ行事計",
                        "ＨＲ",
                        "行事",
                        );
        //ヘッダ配列を出力変数にセット
        $headerContents1 = implode($header, $comma) .$lf;
        $query = knjm391mQuery::getCsvQueryDay($this, $baseYear);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataContents1 .= $row["TITLE"] . ",";
            $dataContents1 .= $row["TOTAL"] . ",";
            $dataContents1 .= $row["IPPAN"] . ",";
            $dataContents1 .= $row["HENNYU"] . ",";
            $dataContents1 .= ",";
            $dataContents1 .= $row["HR_GYOUJI"] . ",";
            $dataContents1 .= $row["HR"] . ",";
            $dataContents1 .= $row["GYOUJI"] . ",";
            $dataContents1 .= ",";
            $dataContents1 .= $row["GRD_HR_GYOUJI"] . ",";
            $dataContents1 .= $row["GRD_HR"] . ",";
            $dataContents1 .= $row["GRD_GYOUJI"] . ",";
            $dataContents1 .= $lf;
            $data = true;
        }
        $dataContents1 .= $lf;

        $query = knjm391mQuery::getCsvQuerySubclass($this);
        $subclassAttend = array();
        $subclasscd = "";
        $current = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($subclasscd != $row["SUBCLASSCD"]) {
                if (!empty($current)) {
                    $subclassAttend[$subclasscd] = $current;
                }
                $current = array();
                $current["SUBCLASSNAME"] = $row["SUBCLASSNAME"];
            }
            $current["PERIOD".$row["PERIODCD"]] = $row["COUNT"];
            $subclasscd = $row["SUBCLASSCD"];
        }
        if (!empty($current)) {
            $subclassAttend[$subclasscd] = $current;
        }

        $period = array();
        $query = knjm391mQuery::getPeriod($this, $baseYear);
        $headerContents2 .= "校時".",";
        $headerContents2 .= "計".",";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $headerContents2 .= $row["NAME1"] . ",";
            $period[] = $row["NAMECD2"];
        }
        $headerContents2 .= $lf;

        foreach ($subclassAttend as $subclasscd => $subclassData) {
            $dataContents2 .= $subclassData["SUBCLASSNAME"] . ",";

            $periodcd = "999999"; // 計
            $dataContents2 .= ($subclassData["PERIOD".$periodcd] ?  $subclassData["PERIOD".$periodcd] : "0"). ",";
            foreach ($period as $periodcd) {
                $dataContents2 .= ($subclassData["PERIOD".$periodcd] ?  $subclassData["PERIOD".$periodcd] : "0"). ",";
            }
            $dataContents2 .= $lf;
            $data = true;
        }
        $dataContents2 .= $lf;

        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        //タイトル
        $title = "スクーリング出席者数".$lf.$lf."日付".",".$this->field["DATE"].$lf.$lf;
        //$title = $this->wareki_year.'　スクーリング出席者数'.$lf;
        //サブタイトル
        $subtitle = "";
        //$subtitle  =  $comma.$comma.$comma.$comma.$comma."";
        //$subtitle .=  $comma.$comma.$comma.common::getDateHenkan($this->date, "ymd").$lf;
        
        $contents  = "";
        $contents .= $title;
        $contents .= $subtitle;
        $contents .= $headerContents1;
        $contents .= $dataContents1;
        $contents .= $headerContents2;
        $contents .= $dataContents2;
        if ($data) {
            //$filename = sprintf("%s.csv", $this->wareki_year . "スクーリング出席者数");
            $filename = sprintf("%s.csv", "スクーリング出席者数");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

}
?>
