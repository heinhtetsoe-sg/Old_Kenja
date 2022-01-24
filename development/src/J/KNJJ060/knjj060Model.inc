<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjj060Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $selectdata = array();
    var $selectsort = array();
    var $schoolName;

    /**
     * メンバ変数の初期化
     */
    function knjj060Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("SCHKIND"              =>  VARS::post("SCHKIND"),              //校種
                             "DATE"                 =>  VARS::post("DATE"),                 //処理年月日
                             "YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "GAKKI"                =>  VARS::post("GAKKI"),                //学期コード
                             "SORT_SELECTED_HIDDEN" =>  VARS::post("SORT_SELECTED_HIDDEN"), //ソート順
                             "FROM_DATE"            =>  VARS::post("FROM_DATE"),            //対象期間From
                             "TO_DATE"              =>  VARS::post("TO_DATE"),              //対象期間To
                             "hogosya"              =>  VARS::post("hogosya"),              //保護者、住所、電話番号
                             "taibusya_nozoku"      =>  VARS::post("taibusya_nozoku"),      //退部者除く
                             "PATTERN"              =>  VARS::post("PATTERN"),              //帳票パターン
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                     );

        if ($this->field["SORT_SELECTED_HIDDEN"]) {
            $this->field["SORT_SELECTED_HIDDEN"] = explode(',', $this->field["SORT_SELECTED_HIDDEN"]);
        } else {
            $this->field["SORT_SELECTED_HIDDEN"] = array(); //ソートのデフォルト値を決めたい場合はここの配列に値をセットしてください。
        }

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");
        $this->selectsort   = VARS::post("selectsort");

        $rtnRow = knjj060Query::getNameMst();
        $this->schoolName   = $rtnRow["NAME1"];
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        if ($this->field["PATTERN"] == '' || $this->field["PATTERN"] == "1") {
            list($data, $contents) = $this->getPatternA($db);
        } else {
            list($data, $contents) = $this->getPatternB($db);
        }

        Query::dbCheckIn($db);

        if ($data){
            $couse = "{$wrk_nendo}　部員名簿一覧（部クラブ単位）";
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
        $title = "部員名簿一覧（部クラブ単位）";
        $wrk_date = common::DateConv1(str_replace("-","/",CTRL_DATE),0);

        $contents  = "{$wrk_nendo}　{$title},,,,,,,,,,{$wrk_date}\n";

        $data = false;
        $dataTemp = array();

        if($this->field["hogosya"] == ""){
            //ヘッダの作成//
            $header = array("CLUBCD"        => "部クラブコード／名称",
                            "HR_NAMEABBV"   => "クラス",
                            "ATTENDNO"      => "出席番号",
                            "NAME"          => "氏名",
                            "NAME_KANA"     => "ふりがな",
                            "BIRTHDAY"      => "生年月日",
                            "SDATE"         => "部活開始日",
                            "EDATE"         => "部活終了日",
                            "EXECUTIVENAME" => "役職",
                            "REMARK"        => "備考",
                            "DETAIL_DATE"   => "記録備考日付",
                            "DETAIL_REMARK" => "記録備考"
                            );

            $contents .= implode($header, ",") ."\n";

            //出力データの作成//
            $result = $db->query(knjj060Query::selectCsvQueryA($this));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $colon = ($row["CLUBCD"] && $row["CLUBNAME"]) ? ":" : "";

                $dataTemp["CLUBCD"]         = $row["CLUBCD"].$colon.$row["CLUBNAME"];
                $dataTemp["HR_NAMEABBV"]    = $row["HR_NAMEABBV"];
                $dataTemp["ATTENDNO"]       = $row["ATTENDNO"];
                $dataTemp["NAME"]           = $row["NAME"];
                $dataTemp["NAME_KANA"]      = $row["NAME_KANA"];
                $dataTemp["BIRTHDAY"]       = ($row["BIRTHDAY"]) ? str_replace("-","/",$row["BIRTHDAY"]) : "";
                $dataTemp["SDATE"]          = ($row["SDATE"]) ? str_replace("-","/",$row["SDATE"]) : "";
                $dataTemp["EDATE"]          = ($row["EDATE"]) ? str_replace("-","/",$row["EDATE"]) : "";
                $dataTemp["EXECUTIVENAME"]  = $row["EXECUTIVENAME"];
                $dataTemp["REMARK"]         = $row["REMARK"];
                $dataTemp["DETAIL_DATE"]    = ($row["DETAIL_DATE"]) ? str_replace("-","/",$row["DETAIL_DATE"]) : "";
                $dataTemp["DETAIL_REMARK"]  = $row["DETAIL_REMARK"];

                $contents .= implode($dataTemp, ",") ."\n";
                $data = true;
            }
            $result->free();
        } else {
            //ヘッダの作成//
            $header = array("CLUBCD"        => "部クラブコード／名称",
                            "HR_NAMEABBV"   => "クラス",
                            "ATTENDNO"      => "出席番号",
                            "NAME"          => "氏名",
                            "NAME_KANA"     => "ふりがな",
                            "EXECUTIVENAME" => "役職",
                            "SDATE"         => "部活開始日",
                            "EDATE"         => "部活終了日",
                            "GUARD_NAME"    => "保護者氏名",
                            "GUARD_ADDR"    => "保護者住所",
                            "GUARD_TELNO"   => "保護者電話番号"
                            );

            $contents .= implode($header, ",") ."\n";

            //出力データの作成//
            $result = $db->query(knjj060Query::selectCsvQueryA($this));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $colon = ($row["CLUBCD"] && $row["CLUBNAME"]) ? ":" : "";

                $dataTemp["CLUBCD"]         = $row["CLUBCD"].$colon.$row["CLUBNAME"];
                $dataTemp["HR_NAMEABBV"]    = $row["HR_NAMEABBV"];
                $dataTemp["ATTENDNO"]       = $row["ATTENDNO"];
                $dataTemp["NAME"]           = $row["NAME"];
                $dataTemp["NAME_KANA"]      = $row["NAME_KANA"];
                $dataTemp["EXECUTIVENAME"]  = $row["EXECUTIVENAME"];
                $dataTemp["SDATE"]          = ($row["SDATE"]) ? str_replace("-","/",$row["SDATE"]) : "";
                $dataTemp["EDATE"]          = ($row["EDATE"]) ? str_replace("-","/",$row["EDATE"]) : "";
                $dataTemp["GUARD_NAME"]     = $row["GUARD_NAME"];
                $dataTemp["GUARD_ADDR"]     = $row["GUARD_ADDR"];
                $dataTemp["GUARD_TELNO"]    = $row["GUARD_TELNO"];

                $contents .= implode($dataTemp, ",") ."\n";
                $data = true;
            }
            $result->free();
        }

        return array($data, $contents);
    }

    //ＣＳＶ出力-B
    function getPatternB($db) {

        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-","/",CTRL_DATE),10);
        $title = "部活・委員名簿";
        $wrk_date = common::DateConv1(str_replace("-","/",CTRL_DATE),0);

        if ($this->field["hogosya"] == ""){
            $contents  = "{$wrk_nendo}　{$title},,,,,,,,,,,{$wrk_date}\n";
        } else {
            $contents  = "{$wrk_nendo}　{$title},,,,,,,,,{$wrk_date}\n";
        }

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
        if ($this->field["hogosya"] == ""){
            $header["GUARD_NAME"]       = "保護者氏名";
            $header["GUARD_KANA"]       = "保護者よみ";
        }
        $header["FINSCHOOLNAME"]    = "出身中学";

        //出力データの作成//
        $clubData = array();
        $result = $db->query(knjj060Query::selectCsvQueryB($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $colon = ($row["CLUBCD"] && $row["CLUBNAME"]) ? ":" : "";

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

            $clubData[$row["CLUBCD"]]["NAME"] = $row["CLUBCD"].$colon.$row["CLUBNAME"];
            $clubData[$row["CLUBCD"]]["DATA"][] = $dataArray;

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
