<?php

require_once('for_php7.php');


//定数定義
class knjl211cModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $selectdata = array();
    var $pre_testdiv;

    //メンバ変数の初期化
    function knjl211cModel()
    {
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;
    }

    function init()
    {
        $this->field = array("SORT_SELECTED_HIDDEN"	=>  VARS::post("SORT_SELECTED_HIDDEN"), //ソート順
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        if ($this->field["SORT_SELECTED_HIDDEN"]) {
            $this->field["SORT_SELECTED_HIDDEN"] = explode(',', $this->field["SORT_SELECTED_HIDDEN"]);
        } else {
            $this->field["SORT_SELECTED_HIDDEN"] = array(); //ソートのデフォルト値を決めたい場合はここの配列に値をセットしてください。
        }

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");
        $this->pre_testdiv  = VARS::post("PRE_TESTDIV");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //ＣＳＶ出力
    function getDownloadModel() {

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル行
        $wrk_nendo = common::DateConv1($this->ObjYear.'/04/01',10);
        $appdiv = $db->getOne(knjl211cQuery::getApctDiv($this));
        $preTestdivRow = $db->getRow(knjl211cQuery::getPreTestdiv($this), DB_FETCHMODE_ASSOC);
        $title = "{$wrk_nendo}　{$appdiv}入試プレテスト　申込者名簿（{$preTestdivRow["CSV_LABEL"]}）";

        $contents  = "{$title}\n";

        $data = false;
        $dataTemp = array();

        //ヘッダの作成//
        $header = array("PRE_RECEPTDATE"    => "No.",
                        "PRE_RECEPTNO"      => "受験番号",
                        "PRE_EXAM_TYPE"     => "受験型",
                        "NAME"              => "氏名",
                        "NAME_KANA"         => "氏名かな",
                        "SEX"               => "性別",
                        "GNAME"             => "保護者氏名",
                        "GKANA"             => "保護者氏名かな",
                        "ZIPCD"             => "郵便番号",
                        "ADDRESS1"          => "住所",
                        "ADDRESS2"          => "方書",
                        "TELNO"             => "電話番号",
                        "FINSCHOOLNAME"     => "出身学校",
                        "PS_CD"             => "塾名",
                        "PS_CONTACT"        => "塾への連絡を希望"
                        );

        //バス情報表示
        if ($this->Properties["Pretest_bus_Not_Hyouji"] != "1") {
            $header["BUS_USER_COUNT3"] = "JR五条駅";
            $header["BUS_USER_COUNT1"] = "林間田園都市駅";
            $header["BUS_USER_COUNT2"] = "福神駅";
        }
        $header["RECOM_EXAMNO"] = "重複受験番号";
        $header["REMARK"] = "備考";

        $contents .= implode($header, ",") ."\n";

        $cnt = 0;
        $BusUserCount3 = 0;
        $BusUserCount1 = 0;
        $BusUserCount2 = 0;
        //出力データの作成//
        $result = $db->query(knjl211cQuery::selectCsvQuery($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $cnt++;
            $dataTemp["PRE_RECEPTDATE"]     = $cnt;
            $dataTemp["PRE_RECEPTNO"]       = $row["PRE_RECEPTNO"];
            $dataTemp["PRE_EXAM_TYPE"]      = $row["PRE_EXAM_TYPE"];
            $dataTemp["NAME"]               = $row["NAME"];
            $dataTemp["NAME_KANA"]          = $row["NAME_KANA"];
            $dataTemp["SEX"]                = $row["SEX"];
            $dataTemp["GNAME"]              = $row["GNAME"];
            $dataTemp["GKANA"]              = $row["GKANA"];
            $dataTemp["ZIPCD"]              = $row["ZIPCD"];
            $dataTemp["ADDRESS1"]           = $row["ADDRESS1"];
            $dataTemp["ADDRESS2"]           = $row["ADDRESS2"];
            $dataTemp["TELNO"]              = $row["TELNO"];
            $dataTemp["FINSCHOOLNAME"]      = $row["FINSCHOOLNAME"];
            $dataTemp["PS_CD"]              = $row["PS_CD"];
            $dataTemp["PS_CONTACT"]         = $row["PS_CONTACT"];

            //バス情報表示
            if ($this->Properties["Pretest_bus_Not_Hyouji"] != "1") {
                $dataTemp["BUS_USER_COUNT3"]    = ($row["BUS_USE"] == "1" && $row["STATIONDIV"] == "3") ? $row["BUS_USER_COUNT"] : "";
                $dataTemp["BUS_USER_COUNT1"]    = ($row["BUS_USE"] == "1" && $row["STATIONDIV"] == "1") ? $row["BUS_USER_COUNT"] : "";
                $dataTemp["BUS_USER_COUNT2"]    = ($row["BUS_USE"] == "1" && $row["STATIONDIV"] == "2") ? $row["BUS_USER_COUNT"] : "";

                //学園バス利用の集計
                $BusUserCount3 += ($dataTemp["BUS_USER_COUNT3"]) ? $dataTemp["BUS_USER_COUNT3"] : 0;
                $BusUserCount1 += ($dataTemp["BUS_USER_COUNT1"]) ? $dataTemp["BUS_USER_COUNT1"] : 0;
                $BusUserCount2 += ($dataTemp["BUS_USER_COUNT2"]) ? $dataTemp["BUS_USER_COUNT2"] : 0;
            }
            $dataTemp["RECOM_EXAMNO"]   = $row["RECOM_EXAMNO"];
            $dataTemp["REMARK"]         = $row["REMARK"];

            $contents .= implode($dataTemp, ",") ."\n";
            $data = true;
        }
        $result->free();

        //学園バス利用の集計出力
        if ($this->Properties["Pretest_bus_Not_Hyouji"] != "1") {
            $contents .= ",,,,,,,,,,,,,,計,".$BusUserCount3.",".$BusUserCount1.",".$BusUserCount2."\n";
        }

        //DB切断
        Query::dbCheckIn($db);

        if ($data){
            $couse = $title;
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
