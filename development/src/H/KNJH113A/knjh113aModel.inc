<?php

require_once('for_php7.php');

class knjh113aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $selectdata = array();

    function knjh113aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("SDATE"    => VARS::post("SDATE"),         //取得資格期間(開始)
                             "EDATE"    => VARS::post("EDATE"),         //取得資格期間(終了)
                             "DBNAME"   => VARS::post("DBNAME"));       //DB変更に備えて

        $this->cmd = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-","/",CTRL_DATE),10);
        $title = "取得資格別一覧表";
        $wrk_sdate = common::DateConv1(str_replace("-","/",$this->field["SDATE"]),0);
        $wrk_edate = common::DateConv1(str_replace("-","/",$this->field["EDATE"]),0);
        $wrk_date = common::DateConv1(str_replace("-","/",CTRL_DATE),0);

        $contents  = "{$wrk_nendo}　{$title},,（取得資格期間　{$wrk_sdate}～{$wrk_edate}）,,,,,,,{$wrk_date}\n";

        $data = false;
        $dataTemp = array();
        //ヘッダの作成//
        $header = array("CONDITION_DIV"     => "資格検定",
                        "QUALIFIED_NAME"    => "資格名称",
                        "PROMOTER"          => "主催者",
                        "HR_NAME"           => "年組",
                        "ATTENDNO"          => "出席番号",
                        "NAME"              => "氏名",
                        "SEX"               => "性別",
                        "RANK"              => "級・段位",
                        "SCORE"             => "得点",
                        "REGDDATE"          => "取得日",
                        "RECEIPT"           => "証書受領",
                        "REMARK"            => "備考"
                        );

        $contents .= implode($header, ",") ."\n";

        //出力データの作成//
        $result = $db->query(knjh113aQuery::selectCsvQuery($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $dataTemp["CONDITION_DIV"]  = $db->getOne(knjh113aQuery::getNameMst('H311', $row["CONDITION_DIV"], "NAME1"));
            $dataTemp["QUALIFIED_NAME"] = $row["QUALIFIED_NAME"];
            $dataTemp["PROMOTER"]       = $row["PROMOTER"];
            $dataTemp["HR_NAME"]        = $row["HR_NAME"];
            $dataTemp["ATTENDNO"]       = $row["ATTENDNO"];
            $dataTemp["NAME"]           = $row["NAME"];
            $dataTemp["SEX"]            = $db->getOne(knjh113aQuery::getNameMst('Z002', $row["SEX"], "NAME2"));
            $dataTemp["RANK"]           = $db->getOne(knjh113aQuery::getNameMst('H312', $row["RANK"], "NAME1"));
            $dataTemp["SCORE"]          = $row["SCORE"];
            $dataTemp["REGDDATE"]       = ($row["REGDDATE"]) ? str_replace("-","/",$row["REGDDATE"]) : "";
            $dataTemp["RECEIPT"]        = ($row["CERTIFICATE"] === "1") ? "済" : "";
            $dataTemp["REMARK"]         = $row["REMARK"];

            $contents .= implode($dataTemp, ",") ."\n";
            $data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse = "{$wrk_nendo}　取得資格別一覧表";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    function clean() {}
}
?>
