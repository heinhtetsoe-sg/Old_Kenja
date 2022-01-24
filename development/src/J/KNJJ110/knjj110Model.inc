<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjj110Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $selectdata = array();

    /**
     * メンバ変数の初期化
     */
    function knjj110Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("SCHKIND"         =>  VARS::post("SCHKIND"),       //校種
                             "DATE"            =>  VARS::post("DATE"),          //処理年月日
                             "YEAR"            =>  VARS::post("YEAR"),          //年度
                             "GAKKI"           =>  VARS::post("GAKKI"),         //学期コード
                             "J004"            =>  VARS::post("J004"),          //対象学期
                             "PATTERN"         =>  VARS::post("PATTERN"),       //帳票パターン
                             "PATTERN2_PRINT_BIRTHDAY" =>  VARS::post("PATTERN2_PRINT_BIRTHDAY"),       //生年月日出力
                             "DBNAME"          =>  VARS::post("DBNAME")         //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
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

        if ($this->field["PATTERN"] == "1") {
            list($data, $contents) = $this->getPatternA($db);
        } else {
            list($data, $contents) = $this->getPatternB($db);
        }

        Query::dbCheckIn($db);

        if ($data){
            $couse = "{$wrk_nendo}　委員会名簿一覧（委員会単位）";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //ＣＳＶ出力-A
    function getPatternA($db) {

        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-","/",CTRL_DATE),10);
        $title = "委員会名簿一覧（委員会単位）";
        $wrk_date = common::DateConv1(str_replace("-","/",CTRL_DATE),0);

        $contents  = "{$wrk_nendo}　{$title},,,,,{$wrk_date}\n";

        $data = false;
        $dataTemp = array();
        //ヘッダの作成//
        $header = array("COMMITTEECD"   => "委員会コード",
                        "COMMITTEENAME" => "委員会名",
                        "SEMESTER"      => "学期",
                        "HR_NAMEABBV"   => "クラス",
                        "ATTENDNO"      => "出席番号",
                        "NAME"          => "氏名"
                        );

        $contents .= implode($header, ",") ."\n";

        $selectdata = explode(',', $this->selectdata);
        for($i = 0; $i < get_count($selectdata); $i++){
            //出力データの作成//
            $result = $db->query(knjj110Query::selectCsvQueryA($this, $selectdata[$i]));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $hyphen = ($row["COMMITTEE_FLG"] && $row["COMMITTEECD"]) ? "-" : "";
                $semester = $db->getOne(knjj110Query::getNameMst('J004', $row["SEMESTER"]));

                $dataTemp["COMMITTEECD"]    = $row["COMMITTEE_FLG"].$hyphen.$row["COMMITTEECD"];
                $dataTemp["COMMITTEENAME"]  = $row["COMMITTEENAME"];
                $dataTemp["SEMESTER"]       = $semester;
                $dataTemp["HR_NAMEABBV"]    = $row["HR_NAMEABBV"];
                $dataTemp["ATTENDNO"]       = $row["ATTENDNO"];
                $dataTemp["NAME"]           = $row["NAME"];

                $contents .= implode($dataTemp, ",") ."\n";
                $data = true;
            }
        }
        $result->free();

        return array($data, $contents);
    }

    //ＣＳＶ出力-B
    function getPatternB($db) {

        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-","/",CTRL_DATE),10);
        $title = "部活・委員名簿";
        $wrk_date = common::DateConv1(str_replace("-","/",CTRL_DATE),0);

        $contents  = "{$wrk_nendo}　{$title},,,,,,,,,,,{$wrk_date}\n";

        $data = false;
        $dataTemp = array();

        //ヘッダの作成//
        $header["HR_NAMEABBV"]      = "クラス";
        $header["ATTENDNO"]         = "出席番号";
        $header["NAME"]             = "氏名";
        $header["NAME_KANA"]        = "ふりがな";
        $header["SCHREGNO"]         = "学籍番号";
        $header["BIRTHDAY"]         = "生年月日";
        $header["SEX"]              = "性別";
        $header["ADDR"]             = "住所";
        $header["TEL"]              = "電話番号";
        $header["GUARD_NAME"]       = "保護者氏名";
        $header["GUARD_KANA"]       = "保護者よみ";
        $header["FINSCHOOLNAME"]    = "出身中学";

        //出力データの作成//
        $clubData = array();
        $result = $db->query(knjj110Query::selectCsvQueryB($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $colon = ($row["COMMITTEECD"] && $row["COMMITTEENAME"]) ? ":" : "";

            $dataArray = array();
            $dataArray["HR_NAMEABBV"]       = $row["HR_NAMEABBV"];
            $dataArray["ATTENDNO"]          = $row["ATTENDNO"];
            $dataArray["NAME"]              = $row["NAME"];
            $dataArray["NAME_KANA"]         = $row["NAME_KANA"];
            $dataArray["SCHREGNO"]          = $row["SCHREGNO"];
            $dataArray["BIRTHDAY"]          = ($row["BIRTHDAY"]) ? str_replace("-","/",$row["BIRTHDAY"]) : "";
            $dataArray["SEX"]               = $row["SEX"];
            $dataArray["ADDR"]              = $row["ADDR"];
            $dataArray["TELNO"]             = $row["TELNO"];
            if($this->field["hogosya"] == ""){
                $dataArray["GUARD_NAME"]        = $row["GUARD_NAME"];
                $dataArray["GUARD_KANA"]        = $row["GUARD_KANA"];
            }
            $dataArray["FINSCHOOL_NAME"]    = $row["FINSCHOOL_NAME"];

            $clubData[$row["COMMITTEECD"]]["NAME"] = $row["COMMITTEECD"].$colon.$row["COMMITTEENAME"];
            $clubData[$row["COMMITTEECD"]]["DATA"][] = $dataArray;

            $data = true;
        }
        $result->free();

        foreach ($clubData as $clubKey => $clubVal) {
            $clubCnt = get_count($clubVal["DATA"]);
            $contents .= "{$clubVal["NAME"]},{$clubCnt}名\n";
            $contents .= implode($header, ",") ."\n";

            foreach ($clubVal["DATA"] as $key => $val) {
                $contents .= implode($val, ",") ."\n";
            }
        }

        return array($data, $contents);
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
