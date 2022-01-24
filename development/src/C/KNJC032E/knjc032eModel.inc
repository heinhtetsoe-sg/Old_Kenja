<?php

require_once('for_php7.php');

class knjc032eModel extends Model {
    var $cmd;
    var $field;

    function knjc032eModel() {
        $this->field = array();
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field["SELECT_CLASS_TYPE"] = VARS::post("SELECT_CLASS_TYPE");  //学級タイプ選択
        if ($this->cmd == "change_radio") {
            $this->field["hr_class"]        = "";   //対象学級
            $this->field["GROUP_HR_CLASS"]  = "";   //対象複式学級
            $this->field["schregno"]        = "";   //対象生徒
        } else if ($this->cmd == "change_class") {
            $this->field["hr_class"]        = VARS::post("HR_CLASS");   //対象学級
            $this->field["GROUP_HR_CLASS"]  = VARS::post("GROUP_HR_CLASS"); //対象複式学級
            $this->field["schregno"]        = "";   //対象生徒
        } else {
            $this->field["hr_class"]        = VARS::post("HR_CLASS");   //対象学級
            $this->field["GROUP_HR_CLASS"]  = VARS::post("GROUP_HR_CLASS"); //対象複式学級
            $this->field["schregno"]        = VARS::post("SCHREGNO");   //対象生徒
        }
        $this->field["MOVE_ENTER"] = VARS::post("MOVE_ENTER");  //移動方向

        if ($this->cmd == "update") {
            $this->field["SELECT_CLASS_TYPE"]   = VARS::post("HIDDEN_SELECT_CLASS_TYPE");
            $this->field["hr_class"]            = VARS::post("HIDDEN_HR_CLASS");
            $this->field["GROUP_HR_CLASS"]      = VARS::post("HIDDEN_GROUP_HR_CLASS");
            $this->field["schregno"]            = VARS::post("HIDDEN_SCHREGNO");
            $this->field["MOVE_ENTER"]          = VARS::post("HIDDEN_MOVE_ENTER");
        }

        //学年・学級に分割
        list($this->field["grade"], $this->field["class"]) = explode("-", $this->field["hr_class"]);

        //校種取得
        $this->school_kind = knjc032eQuery::getSchoolKind($this);

        //編集対象データを取得
        $this->field["MONTH"]           = VARS::post("MONTH");          //対象月
        $this->field["APPOINTED_DAY"]   = VARS::post("APPOINTED_DAY");  //締め日
        $this->field["LESSON"]          = VARS::post("LESSON");         //授業日数
        $this->field["OFFDAYS"]         = VARS::post("OFFDAYS");        //休学日数
        $this->field["ABROAD"]          = VARS::post("ABROAD");         //留学日数
        $this->field["ABSENT"]          = VARS::post("ABSENT");         //公欠日数
        $this->field["SUSPEND"]         = VARS::post("SUSPEND");        //出停日数
        $this->field["VIRUS"]           = VARS::post("VIRUS");          //伝染病
        $this->field["KOUDOME"]         = VARS::post("KOUDOME");        //交止
        $this->field["MOURNING"]        = VARS::post("MOURNING");       //忌引日数
        $this->field["SICK"]            = VARS::post("SICK");           //病欠
        $this->field["NOTICE"]          = VARS::post("NOTICE");         //事故欠届
        $this->field["NONOTICE"]        = VARS::post("NONOTICE");       //事故欠無
        $this->field["LATE"]            = VARS::post("LATE");           //遅刻回数
        $this->field["EARLY"]           = VARS::post("EARLY");          //早退回数
        $this->field["DETAIL_001"]      = VARS::post("DETAIL_001");     //DETAIL
        $this->field["DETAIL_002"]      = VARS::post("DETAIL_002");     //DETAIL
        $this->field["DETAIL_003"]      = VARS::post("DETAIL_003");     //DETAIL
        $this->field["DETAIL_004"]      = VARS::post("DETAIL_004");     //DETAIL
        $this->field["REMARK"]          = VARS::post("REMARK");         //備考

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //フィールド名
        $this->a004Field["2"]   = "OFFDAYS";        //休学日数
        $this->a004Field["1"]   = "ABROAD";         //留学日数
        $this->c001Field["1"]   = "ABSENT";         //公欠日数
        $this->c001Field["2"]   = "SUSPEND";        //出停日数
        $this->c001Field["25"]  = "KOUDOME";        //交止
        $this->c001Field["19"]  = "VIRUS";          //伝染病
        $this->c001Field["3"]   = "MOURNING";       //忌引日数
        $this->c001Field["4"]   = "SICK";           //病欠
        $this->c001Field["5"]   = "NOTICE";         //事故欠届
        $this->c001Field["6"]   = "NONOTICE";       //事故欠無
        $this->c001Field["15"]  = "LATE";           //遅刻回数
        $this->c001Field["16"]  = "EARLY";          //早退回数
        $this->c002Field["001"] = "DETAIL_001";     //DETAIL
        $this->c002Field["002"] = "DETAIL_002";     //DETAIL
        $this->c002Field["003"] = "DETAIL_003";     //DETAIL
        $this->c002Field["004"] = "DETAIL_004";     //DETAIL

        //フィールド順(コピー貼付用)
        $this->a004FieldSort["2"]   = "1";  //休学日数
        $this->a004FieldSort["1"]   = "2";  //留学日数
        $this->c001FieldSort["1"]   = "3";  //公欠日数
        $this->c001FieldSort["2"]   = "4";  //出停日数
        $this->c001FieldSort["25"]  = "5";  //交止
        $this->c001FieldSort["19"]  = "6";  //伝染病
        $this->c001FieldSort["3"]   = "7";  //忌引日数
        $this->c001FieldSort["4"]   = "8";  //病欠
        $this->c001FieldSort["5"]   = "9";  //事故欠届
        $this->c001FieldSort["6"]   = "10"; //事故欠無
        $this->c001FieldSort["15"]  = "12"; //遅刻回数
        $this->c001FieldSort["16"]  = "13"; //早退回数
        $this->c002FieldSort["001"] = "14"; //DETAIL
        $this->c002FieldSort["002"] = "15"; //DETAIL
        $this->c002FieldSort["003"] = "16"; //DETAIL
        $this->c002FieldSort["004"] = "17"; //DETAIL
    }

    //入力項目チェック
    function validateInput() {
        $db = Query::dbCheckOut();

        //更新対象データがない場合
        if ($this->field["MONTH"] == NULL || $this->field["MONTH"] == "") {
            $this->setWarning("MSG303");
            return false;
        }

        foreach ($this->field["MONTH"] as $key => $month) {
            //$monthAr[0] = 月、$monthAr[1] = 学期
            $monthAr = preg_split("/-/", $month);

            if ($this->dataCheck($key) ||
                (!$this->dataCheck($key) && "" != $this->field["APPOINTED_DAY"][$key])) {
                $errMsg = $this->appDateCheck($db, $key, $monthAr);
                if ("" != $errMsg) {
                    $this->setWarning("MSG901",$errMsg);
                    return false;
                }
            }
        }

        Query::dbCheckIn($db);

        return true;
    }

    //入力データ判定
    function dataCheck($key) {
        if ($this->field["LESSON"][$key]    != "" ||
            $this->field["OFFDAYS"][$key]   != "" ||
            $this->field["ABROAD"][$key]    != "" ||
            $this->field["ABSENT"][$key]    != "" ||
            $this->field["SUSPEND"][$key]   != "" ||
            $this->field["KOUDOME"][$key]   != "" ||
            $this->field["VIRUS"][$key]     != "" ||
            $this->field["MOURNING"][$key]  != "" ||
            $this->field["SICK"][$key]      != "" ||
            $this->field["NOTICE"][$key]    != "" ||
            $this->field["NONOTICE"][$key]  != "" ||
            $this->field["LATE"][$key]      != "" ||
            $this->field["EARLY"][$key]     != "")
        {
            return true;
        }
        return false;
    }

    //締め日チェック
    function appDateCheck($db, $key, $monthAr) {
        //締め日の最終日
        $year = CTRL_YEAR;
        if ($monthAr[0] < "04") {
            $year = CTRL_YEAR + 1;
        }
        $lastday = date("t", mktime( 0, 0, 0, $monthAr[0], 1, $year ));
        //締め日がその月の最終日より大きい場合
        if ($lastday < $this->field["APPOINTED_DAY"][$key]) {
            return $monthAr[0]."月の最終日は、".$lastday."日です。";
        }
        //締め日がNullの場合
        if ("" == $this->field["APPOINTED_DAY"][$key]) {
            return "日付が入力されていません。(".$monthAr[0]."月)";
        }
        //締め日が0の場合
        if ("" != $this->field["APPOINTED_DAY"][$key] && 0 == $this->field["APPOINTED_DAY"][$key]) {
            return "0日は存在しません。(".$monthAr[0]."月)";
        }
        $semeday = $db->getRow(knjc032eQuery::selectSemesAll($monthAr[1]),DB_FETCHMODE_ASSOC);
        //学期マスタの開始日より小さい場合
        if (sprintf('%02d', $semeday["S_MONTH"]) == $monthAr[0] &&
            $semeday["S_DAY"] > $this->field["APPOINTED_DAY"][$key]) {
            return $semeday["SEMESTERNAME"]."の開始日は、".$semeday["S_MONTH"]."月".$semeday["S_DAY"]."日です。";
        }
        //学期マスタの最終日より大きい場合
        if (sprintf('%02d', $semeday["E_MONTH"]) == $monthAr[0] &&
            $semeday["E_DAY"] < $this->field["APPOINTED_DAY"][$key]) {
            return $semeday["SEMESTERNAME"]."の最終日は、".$semeday["E_MONTH"]."月".$semeday["E_DAY"]."日です。";
        }
        return "";
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()){
            if($this->update()){
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //編集対象データを更新
    function &update() {
        $result = knjc032eQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
