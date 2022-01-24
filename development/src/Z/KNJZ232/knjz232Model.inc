<?php

require_once('for_php7.php');

class knjz232Model extends Model {
    var $cmd;
    var $subclasscd;        //primary
    //教育課程用
    var $classcd;        //primary
    var $school_kind;        //primary
    var $curriculum_cd;        //primary
    
    var $grade = "";
    var $record_dat_flg = "0";
    var $control = array();
    var $org_data;
    var $selectdata;
    var $rightclasscd;  //右画面の教科コンボ

    //メンバ変数の初期化
    function knjz232Model()
    {
    }

    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        
        if (VARS::request("SUBCLASSCD")) {
            $this->subclasscd = VARS::request("SUBCLASSCD");
        }
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (VARS::request("CLASSCD")) {
                $this->classcd = VARS::request("CLASSCD");
            }
            if (VARS::request("SUBCLASSCD")) {
                $this->school_kind = VARS::request("SCHOOL_KIND");
            }
            if (VARS::request("CURRICULUM_CD")) {
                $this->curriculum_cd = VARS::request("CURRICULUM_CD");
            }
        }
        
        if (VARS::get("PROGRAMID")) {
            unset($this->grade);
            unset($this->subclasscd);
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                unset($this->classcd);
                unset($this->check_classcd);
                unset($this->school_kind);
                unset($this->curriculum_cd);
            }
        }
        if (VARS::request("grade") != "") {
            $this->grade  = VARS::request("grade");
        }
        if ($this->cmd == "check"){
            $this->selectdata   = VARS::post("selectdata");
            $this->grading      = VARS::post("grading_flg");
        }
        if (strlen(VARS::post("classcd"))) $this->classcd = VARS::post("classcd");
        $this->cmd              = VARS::request("cmd");
        $this->rightclasscd     = VARS::post("rightclasscd");
        $this->record_dat_flg   = VARS::post("record_dat_flg");
    }

    function &getUpdateModel()
    {
        //データの変更がない場合は処理をしない
        if (isset($this->subclasscd) && implode(",",$this->org_data) == $this->selectdata){
            $this->warning = "読替え科目は変更されていません。";
            return;
        }
        //更新時の重複チェック
        $cntgradingcd = knjz232Query::IsExisting(CTRL_YEAR, $this->grade, $this->selectdata, $this->subclasscd, $this->org_data, $this);
        if($cntgradingcd > 0){
            $this->warning = "読替え科目に登録されています。";
            return;
        }
#2005/05/19
#        //更新時の重複チェック
#        if($this->selectdata != ''){
#            $doublegradingcd = knjz232Query::ExistDouble(CTRL_YEAR, $this->grade, $this->subclasscd);
#            if($doublegradingcd > 0){
#                $this->warning = "合併登録されています。";
#                return;
#            }
#        }
        //更新しようとしている科目は読替え元になっていないかチェック
        $array_selectdata = explode(",",$this->selectdata); //読替え科目一覧にある科目
        $existgradingcd = knjz232Query::ExistGradingcd(CTRL_YEAR, $this->grade, $this->subclasscd); //既に読替えられている科目
        for($i=0; $i<get_count($existgradingcd); $i++){
            for($j=0; $j<get_count($array_selectdata); $j++){
                if($existgradingcd[$i] == $array_selectdata[$j]){
                    $this->warning = "読替えられています。";
                    return;
                }
            }
        }
        $this->update();
    }

    function &update()
    {
        knjz232Query::getUpdateQuery($this);

        if($this->CNT != '0')   $this->setMessage("MSG201","読替え処理をしていれば再処理を行ってください。");
        else                    $this->setMessage("MSG201");

        return;
    }
}
?>
