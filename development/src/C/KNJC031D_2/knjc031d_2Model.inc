<?php

require_once('for_php7.php');

class knjc031d_2Model extends Model {

    var $cmd;
    var $field = array();
    var $reset = array();
    var $auth;
    var $hr_class_type;
    var $grade;
    var $hr_class;
    var $month;
    var $appointed_day;
    var $title;

    function knjc031d_2Model()
    {
        $this->field = array();
    }

    function init()
    {
        if (VARS::get("prgid")) {
            unset($this->auth);
            unset($this->hr_class_type);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->month);
            unset($this->appointed_day);
            unset($this->title);
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み

        if(VARS::get("auth") != ""){
            $this->auth    = VARS::get("auth");
        }
        if(VARS::get("HR_CLASS_TYPE") != ""){
            $this->hr_class_type    = VARS::get("HR_CLASS_TYPE");
        }
        if(VARS::get("GRADE") != ""){
            $this->grade    = VARS::get("GRADE");
        }
        if(VARS::get("HR_CLASS") != ""){
            $this->hr_class = VARS::get("HR_CLASS");
        }
        if(VARS::get("MONTH") != ""){
            $this->month    = VARS::get("MONTH");
        }
        if(VARS::get("APPOINTED_DAY") != ""){
            $this->appointed_day    = VARS::get("APPOINTED_DAY");
        }
        if(VARS::get("TITLE") != ""){
            $this->title    = VARS::get("TITLE");
        }

        $this->cmd = VARS::request("cmd");

        /* 編集対象データを取得 */
        $this->field["SCHREGNO"]    = VARS::post("SCHREGNO");   //学籍番号
        $this->field["DI_CD"]       = VARS::post("DI_CD");      //出欠コード
        $this->field["DI_NAME"]     = VARS::post("DI_NAME");    //出欠項目名

        $this->notUpdateItem        = VARS::post("NOT_UPDATE_ITEM");

        $db = Query::dbCheckOut();
        $namecd1 = (substr($this->title,0,3) == 'CNT') ? 'C007' : 'C006';
        $namespare1 = (substr($this->title,0,3) == 'CNT') ? substr($this->title,3) : $this->field["DI_CD"];
        $sub_data = $db->getCol(knjc031d_2Query::getNameMst($namecd1, $namespare1));
        foreach($sub_data as $subkey) {
            $this->field["CNT".$subkey] = VARS::post("CNT".$subkey);
        }
        Query::dbCheckIn($db);

        $this->reset = array();
        if (is_array($this->field["SCHREGNO"])) {
            foreach ($this->field["SCHREGNO"] as $key => $schregno) {
                foreach($sub_data as $subkey) {
                        $this->reset[$schregno]["CNT".$subkey] = $this->field["CNT".$subkey][$key];
                }
            }
        }
    }

    /* 入力項目チェック */
    function validateInput()
    {
        $db = Query::dbCheckOut();

        if(!isset($this->field["SCHREGNO"])){
            $this->setMessage("MSG304");
            return false;
        }

        $monthAr = preg_split("/-/", $this->month);

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

        $semeday = $db->getRow(knjc031d_2Query::selectSemesAll($monthAr[1]),DB_FETCHMODE_ASSOC);
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

        Query::dbCheckIn($db);

        return true;
    }

    /* 更新処理 */
    function &getUpdateModel()
    {
        if ($this->validateInput()){
            if($this->update()){
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    /* 編集対象データを更新 */
    function &update()
    {
        $result = knjc031d_2Query::getUpdateQuery($this);
        return $result;
    }
}
?>
