<?php

require_once('for_php7.php');

class knjc031eModel extends Model {

    var $cmd;
    var $field = array();
    var $reset = array();
    var $appointed_day;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knjc031eModel()
    {
        $this->field = array();
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd               = VARS::request("cmd");
        $this->appointed_day     = VARS::post("SET_APPOINTED_DAY");     //締め日
        $this->field["SELECT_CLASS_TYPE"] = VARS::post("SELECT_CLASS_TYPE");  //学級タイプ選択
        if ($this->cmd == "change_radio") {
            $this->field["hr_class"]        = "";   //対象学級
            $this->field["GROUP_HR_CLASS"]  = "";   //対象複式学級
        } else {
            $this->field["hr_class"]        = VARS::post("HR_CLASS");       //対象学級
            $this->field["GROUP_HR_CLASS"]  = VARS::post("GROUP_HR_CLASS"); //対象複式学級
        }
        $this->field["month"]       = VARS::post("MONTH");      //対象月
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //移動方向
        $this->field["LESSON_SET"]  = VARS::post("LESSON_SET"); //授業日数(セット用)
        $this->field["HR_REMARK"]   = VARS::post("HR_REMARK");  //クラス備考

        if ($this->cmd == "update") {
            $this->field["SELECT_CLASS_TYPE"]   = VARS::post("HIDDEN_SELECT_CLASS_TYPE");
            $this->field["hr_class"]            = VARS::post("HIDDEN_HR_CLASS");
            $this->field["GROUP_HR_CLASS"]      = VARS::post("HIDDEN_GROUP_HR_CLASS");
            $this->field["month"]               = VARS::post("HIDDEN_MONTH");
            $this->field["LESSON_SET"]          = VARS::post("HIDDEN_LESSON_SET");
            $this->field["MOVE_ENTER"]          = VARS::post("HIDDEN_MOVE_ENTER");
        }

        /* 学年・学級に分割 */
        list($this->field["grade"], $this->field["class"]) = explode("-", $this->field["hr_class"]);

        /* 編集対象データを取得 */
        $this->field["SCHREGNO"]    = VARS::post("SCHREGNO");   //学籍番号
        $this->field["LESSON"]      = VARS::post("LESSON");     //授業日数
        $this->field["OFFDAYS"]     = VARS::post("OFFDAYS");    //休学日数
        $this->field["ABROAD"]      = VARS::post("ABROAD");     //留学日数
        $this->field["ABSENT"]      = VARS::post("ABSENT");     //公欠日数
        $this->field["SUSPEND"]     = VARS::post("SUSPEND");    //出停日数
        $this->field["KOUDOME"]     = VARS::post("KOUDOME");    //交止
        $this->field["VIRUS"]       = VARS::post("VIRUS");      //伝染病
        $this->field["MOURNING"]    = VARS::post("MOURNING");   //忌引日数
        $this->field["SICK"]        = VARS::post("SICK");       //欠席
        $this->field["NOTICE"]      = VARS::post("NOTICE");     //事故欠届
        $this->field["NONOTICE"]    = VARS::post("NONOTICE");   //事故欠無
        $this->field["LATE"]        = VARS::post("LATE");       //遅刻回数
        $this->field["EARLY"]       = VARS::post("EARLY");      //早退回数
        $this->field["DETAIL_001"]  = VARS::post("DETAIL_001"); //DETAIL
        $this->field["DETAIL_002"]  = VARS::post("DETAIL_002"); //DETAIL
        $this->field["DETAIL_003"]  = VARS::post("DETAIL_003"); //DETAIL
        $this->field["DETAIL_004"]  = VARS::post("DETAIL_004"); //DETAIL
        $this->field["DETAIL_101"]  = VARS::post("DETAIL_101"); //DETAIL
        $this->field["REMARK"]      = VARS::post("REMARK");     //備考

        $this->reset = array();
        if (is_array($this->field["SCHREGNO"])) {
            foreach ($this->field["SCHREGNO"] as $key => $schregno) {
                $this->reset[$schregno] = array("LESSON"        => $this->field["LESSON"][$key],
                                                "OFFDAYS"       => $this->field["OFFDAYS"][$key],
                                                "ABROAD"        => $this->field["ABROAD"][$key],
                                                "ABSENT"        => $this->field["ABSENT"][$key],
                                                "SUSPEND"       => $this->field["SUSPEND"][$key],
                                                "KOUDOME"       => $this->field["KOUDOME"][$key],
                                                "VIRUS"         => $this->field["VIRUS"][$key],
                                                "MOURNING"      => $this->field["MOURNING"][$key],
                                                "SICK"          => $this->field["SICK"][$key],
                                                "NOTICE"        => $this->field["NOTICE"][$key],
                                                "NONOTICE"      => $this->field["NONOTICE"][$key],
                                                "LATE"          => $this->field["LATE"][$key],
                                                "EARLY"         => $this->field["EARLY"][$key],
                                                "DETAIL_001"    => $this->field["DETAIL_001"][$key],
                                                "DETAIL_002"    => $this->field["DETAIL_002"][$key],
                                                "DETAIL_003"    => $this->field["DETAIL_003"][$key],
                                                "DETAIL_004"    => $this->field["DETAIL_004"][$key],
                                                "DETAIL_101"    => $this->field["DETAIL_101"][$key],
                                                "REMARK"        => $this->field["REMARK"][$key]);
            }
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
        $this->c002Field["101"] = "DETAIL_101";     //DETAIL

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
        $this->c002FieldSort["101"] = "18"; //DETAIL

        //累計用
        $this->c001Ruikei["15"]  = "1"; //遅刻回数
        $this->c001Ruikei["16"]  = "1"; //早退回数
        $this->c001Ruikei["25"]  = "1"; //交止
        $this->c001Ruikei["19"]  = "1"; //伝染病
        $this->c002Ruikei["001"] = "1"; //DETAIL
        $this->c002Ruikei["002"] = "1"; //DETAIL
        $this->c002Ruikei["003"] = "1"; //DETAIL
        $this->c002Ruikei["004"] = "1"; //DETAIL
        $this->c002Ruikei["101"] = "1"; //DETAIL

        $db = Query::dbCheckOut();

        //A004
        $this->a004 = array();
        $query = knjc031eQuery::getA004();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->a004[$row["NAMECD2"]] = $this->a004Field[$row["NAMECD2"]];
        }
        $result->free();

        //C001
        $this->c001 = array();
        $query = knjc031eQuery::getC001();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->c001[$row["NAMECD2"]] = $this->c001Field[$row["NAMECD2"]];
        }
        $result->free();

        //C002
        $this->c002 = array();
        $query = knjc031eQuery::getDetailDiv();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->c002[$row["VALUE"]] = $this->c002Field[$row["VALUE"]];
        }
        $result->free();

        Query::dbCheckIn($db);
    }

    /* 入力項目チェック */
    function validateInput()
    {
        $db = Query::dbCheckOut();

        if(!isset($this->field["SCHREGNO"])){
            $this->setMessage("MSG304");
            return false;
        }

        $monthAr = preg_split("/-/", $this->field["month"]);

        //締め日の最終日
        $year = CTRL_YEAR;
        if ($monthAr[0] < "04") {
            $year = CTRL_YEAR + 1;
        }
        $lastday = date("t", mktime( 0, 0, 0, $monthAr[0], 1, $year ));
        //締め日がその月の最終日より大きい場合
        if ($lastday < $this->appointed_day) {
            $this->setWarning("MSG901",$monthAr[0]."月の最終日は、".$lastday."日です。");
            return false;
        }
        //締め日が0の場合
        if (0 == $this->appointed_day) {
            $this->setWarning("MSG901","0日は存在しません。");
            return false;
        }

        $semeday = $db->getRow(knjc031eQuery::selectSemesAll($monthAr[1]),DB_FETCHMODE_ASSOC);
        //学期マスタの開始日より小さい場合
        if (sprintf('%02d', $semeday["S_MONTH"]) == $monthAr[0] &&
            $semeday["S_DAY"] > $this->appointed_day) {
            $this->setWarning("MSG901",$semeday["SEMESTERNAME"]."の開始日は、".$semeday["S_MONTH"]."月".$semeday["S_DAY"]."日です。");
            return false;
        }
        //学期マスタの最終日より大きい場合
        if (sprintf('%02d', $semeday["E_MONTH"]) == $monthAr[0] &&
            $semeday["E_DAY"] < $this->appointed_day) {
            $this->setWarning("MSG901",$semeday["SEMESTERNAME"]."の最終日は、".$semeday["E_MONTH"]."月".$semeday["E_DAY"]."日です。");
            return false;
        }

        //文字数チェック
        $err = false;
        $moji = 30;
        foreach($this->field["SCHREGNO"] as $key => $schregno) {
            if (strlen($this->field["REMARK"][$key]) > $moji * 3) {
                $err = true;
            }
        }
        if ($err) {
            $this->setWarning("MSG915","出欠の備考は全角{$moji}文字までです。");
            return false;
        }

        Query::dbCheckIn($db);

        return true;
    }

    /* 更新処理 */
    function &getUpdateModel()
    {
        if ($this->validateInput()){
            if($this->update()){
                unset($this->field["LESSON_SET"]);
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    /* 編集対象データを更新 */
    function &update()
    {
        $result = knjc031eQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
