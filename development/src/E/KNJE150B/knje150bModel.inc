<?php

require_once('for_php7.php');

class knje150bModel extends Model {
    var $field = array();
    var $cmd;
    var $selectdata;
    var $selectdataText;

    function knje150bModel() {}

    function init() {
        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DISP"             =>  VARS::post("DISP"),             //クラス生徒区分
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdataText   = VARS::post("selectdataText");

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //CSV出力
    function getCsvOutputModel() {
        // 項目
        $header = array(
            "SCHREGNO"      => "学籍番号",
            "NAME"          => "氏名",
            "NAME_KANA"     => "氏名かな",
            "BIRTHDAY"      => "生年月日",
            "SEX"           => "性別",
            "HR_NAME"       => "クラス名称",
            "ATTENDNO"      => "クラス出席番号",
            "ADDR1"         => "住所１",
            "ADDE2"         => "住所２",
            "YEAR"          => "年度",
            "SUBCLASSCD"    => "科目コード",
            "CLASSNAME"     => "教科名",
            "SUBCLASSNAME"  => "科目名",
            "VALUATION"     => "評定",
            "VIEWCD1"       => "観点１コード",
            "VIEW1"         => "観点１値",
            "VIEWCD2"       => "観点２コード",
            "VIEW2"         => "観点２値",
            "VIEWCD3"       => "観点３コード",
            "VIEW3"         => "観点３値",
            "VIEWCD4"       => "観点４コード",
            "VIEW4"         => "観点４値",
            "VIEWCD5"       => "観点５コード",
            "VIEW5"         => "観点５値",
        );

        $header_data = false;   //出力フラグ

        $contents  = "";    //クリア

        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        $query = knje150bQuery::getCsvDat($this);
        $result = $db->query($query);
        $prevSchregno = "";
        $prevSubclasscd = "";
        // プロパティ"kantenHyouji = 6"のとき、観点は6つ表示
        if ($this->Properties["kantenHyouji"] == "6") {
            $maxViewCnt = 6;
        } else {
            $maxViewCnt = 5;
        }
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(","   , "." , $row);  //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\r\n", "↓", $row);
            $row = str_replace("\r"  , "↓", $row);
            $row = str_replace("\n"  , "↓", $row);
            if ($prevSchregno != $row["SCHREGNO"] || $prevSubclasscd != $row["SUBCLASSCD"]) {
                if ($setData) {
                    $contents .= implode($setData, ",") ."\n";
                    $header_data = true;
                }
                $viewCnt = 1;
                $setData = array();
                $setData["SCHREGNO"] = $row["SCHREGNO"];
                $setData["NAME"] = $row["NAME"];
                $setData["NAME_KANA"] = $row["NAME_KANA"];
                $setData["BIRTHDAY"] = $row["BIRTHDAY"];
                $setData["SEX"] = $row["SEX"];
                $setData["HR_NAME"] = $row["HR_NAME"];
                $setData["ATTENDNO"] = $row["ATTENDNO"];
                $setData["ADDR1"] = $row["ADDR1"];
                $setData["ADDR2"] = $row["ADDR2"];
                $setData["YEAR"] = $row["YEAR"];
                $setData["SUBCLASSCD"] = $row["SUBCLASSCD"];
                $setData["CLASSNAME"] = $row["CLASSNAME"];
                $setData["SUBCLASSNAME"] = $row["SUBCLASSNAME"];

                //評定
                //選択科目は'11' => A, '22' => B, '33' => C
                if ($row["ELECTDIV"] == "1") {
                    if ($row["VALUATION"] == "11") {
                            $setData["VALUATION"] = "A";
                    } else if ($row["VALUATION"] == "22") {
                        $setData["VALUATION"] = "B";
                    } else if ($row["VALUATION"] == "33") {
                        $setData["VALUATION"] = "C";
                    } else {
                        $setData["VALUATION"] = "";
                    }
                } else {
                    $setData["VALUATION"] = $row["VALUATION"];
                }

                //観点を初期化
                for ($i = 1; $i <= $maxViewCnt; $i++) {
                    $setData["VIEWCD".$i] = "";
                    $setData["VIEW".$i] = "";
                }
            }

            //観点
            $setData["VIEWCD".$viewCnt] = $row["VIEWCD"];
            $setData["VIEW".$viewCnt] = $row["VIEW"];

            $prevSchregno = $row["SCHREGNO"];
            $prevSubclasscd = $row["SUBCLASSCD"];
            $viewCnt++;
        }
        if ($setData) {
            $contents .= implode($setData, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //CSV出力
        if ($header_data) {
            $couse = "指導要録観点データ";

            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>