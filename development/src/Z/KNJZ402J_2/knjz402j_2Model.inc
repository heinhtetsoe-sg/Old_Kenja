<?php

require_once('for_php7.php');

//定数定義

class knjz402j_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $subclasscd = "";    //primary
    var $viewcd="";        //primary
    var $year_code = "";
    //教育課程対応
    var $classcd = "";
    var $school_kind = "";
    var $curriculum_cd = "";

    //メンバ変数の初期化
    function knjz402j_2Model()
    {
    }

    function init()
    {
        $mid ="" ;
        //コードのゼロ埋め
        if (VARS::post("VIEWCD") != "") {
             $mid = sprintf("%02d",VARS::post("VIEWCD"));
        }

        $this->field = array("CLASSCD"          => VARS::post("CLASSCD"),               //教科
                             //教育課程用
                             "CURRICULUM_CD"    => VARS::post("CURRICULUM_CD"),         //教育課程
                             
                             "SUBCLASSCD"       => VARS::post("SUBCLASSCD"),            //科目
                             "VIEWCD"           => VARS::post("CLASSCD").$mid,          //観点コード
                             "VIEWNAME"         => VARS::post("VIEWNAME"),              //観点名
                             "VIEWABBV"         => VARS::post("VIEWABBV"),              //観点略称
                             "SCHOOL_KIND"      => VARS::post("SCHOOL_KIND"),           //学校種別
                             "SHOWORDER"        => VARS::post("SHOWORDER"),             //表示順
                             "WEIGHT"           => VARS::post("WEIGHT"),                //重み
                             "UPDATED"          => VARS::post("UPDATED"),               //更新日付
                             "year_code"        => VARS::post("year_code")              //前画面の年度情保持
                            );

        $this->cmd             = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }

        if (VARS::get("year_code")){
            unset($this->subclasscd);
            unset($this->viewcd);
            unset($this->schkind);
        }
        if (VARS::get("mode") == "1"){
            unset($this->subclasscd);
            unset($this->viewcd);
            unset($this->schkind);
        }
        if (VARS::get("SUBCLASSCD") != ""){
            $this->subclasscd = VARS::get("SUBCLASSCD");
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->classcd = VARS::get("CLASSCD");
                $this->school_kind = VARS::get("SCHOOL_KIND");
                $this->curriculum_cd = VARS::get("CURRICULUM_CD");
            }
        }
        if (VARS::get("VIEWCD")!=""){
            $this->viewcd = VARS::get("VIEWCD");
        }
        //教育課程対応 VIEWCD加工用 ※もともとpostするときにclasscd+viewcdにしているため、教育課程の時おかしくなる
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (strlen($this->viewcd) > 4) {
                $this->viewcd = substr($this->classcd, 0, 2).substr($this->viewcd, 4, 2);
            }
            if (strlen($this->field["VIEWCD"]) > 4) {
                $this->field["VIEWCD"] = substr($this->field["CLASSCD"], 0, 2).substr($this->field["VIEWCD"], 4, 2);
            }
        }
        //左画面
        if (VARS::post("SCHKIND")) {
            $this->schkind = VARS::post("SCHKIND");
        }
    }

    //入力項目チェック
    function validateInput($kind)
    {
        $db = Query::dbCheckOut();
        //１レコード取得
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $row = $db->getRow(knjz402j_2Query::getRow2($this->field["SUBCLASSCD"], $this->field["VIEWCD"], $this), DB_FETCHMODE_ASSOC);
        } else {
            $row = $db->getRow(knjz402j_2Query::getRow($this->field["SUBCLASSCD"], $this->field["VIEWCD"], $this), DB_FETCHMODE_ASSOC);
        }
        
        if ($kind=="update" || $kind=="delete") {                 //リストより選択していない
            if ($this->viewcd==""){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["VIEWCD"] != $this->viewcd){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            //更新日付チェック1
            if ($this->cmd == "update" && $row["UPDATED"] == ""){
                $this->setWarning("MSG303");
                return false;
            }
            //更新日付チェック2
            if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
                $this->setWarning("MSG307");
                return false;
            }
        }
        if ($kind=="update" || $kind=="insert") {
            if (trim($this->field["VIEWCD"]) < 100) {
                $this->setWarning("MSG301","(観点コード)");
                return false;
            }
            if (substr($this->field["VIEWCD"] ,2) == 99) {
                $this->setWarning("MSG901","(観点コード：99は使用不可です。99以外を入力して下さい。)");
                return false;
            }
            //重複チェック
            if ($this->cmd == "add" && is_array($row)){
                $this->setWarning("MSG918","(観点コード)");
                return false;
            }
            if (strlen($this->field["VIEWNAME"]) >75 ) {
                $this->setWarning("MSG915","(観点名は75バイトまでです)");
                return false;
            } elseif (strlen($this->field["VIEWNAME"])==0){
                $this->setWarning("MSG301","(観点名称)");
                return false;
            } elseif (strlen($this->field["VIEWABBV"]) > 48){
                $this->setWarning("MSG915","(観点略称は48バイトまでです)");
                return false;
            } elseif (strlen($this->field["SHOWORDER"]) >2){ 
                $this->setWarning("MSG915","(表示順)");
                return false;
            } 
            //観点の重みチェック
            $getWeightSum = $db->getOne(knjz402j_2Query::getSumWeight($this));
            if ((int)$this->field["WEIGHT"] + $getWeightSum > 100) { 
                $this->setWarning("MSG203","重みの合計が100を超えています。");
                return false;
            }

            if ($this->field["SCHOOL_KIND"] == ""){
                $this->setWarning("MSG301","(学校種別)");
                return false;
            }
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                if (substr($this->field["SUBCLASSCD"], 3, 1) != $this->field["SCHOOL_KIND"]) {
                    $this->setWarning("MSG901","学校種別が一致していません。");
                    return false;
                }
            }
        }
        Query::dbCheckIn($db);
        return true;
    }
    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            $this->update();
        }
    }
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            $this->insert();
            $this->subclasscd = $this->field["SUBCLASSCD"];
            $this->viewcd = $this->field["VIEWCD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            $this->delete();
        }
        return;
    } 
    function &delete()
    {
        if (!isset($this->viewcd)) {
            $this->setWarning("MSG303");
            return;
        }
        $result = knjz402j_2Query::getDeleteQuery($this->field, $this);
        if ($result==true) {
            if ( !isset($model->warning)){
                $this->setMessage("MSG201");
            }
            $this->viewcd ="";
        } else {
            $this->setWarning("MSG919","年度データに登録されています。");
        }
        return $result;
    }
    function &insert()
    {
        $result = knjz402j_2Query::getInsertQuery($this->field, $this);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz402j_2Query::getUpdateQuery($this->field, $this);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
/*        if ($result==true){
            //更新は成功すると、新しい日付を保留
            $row = knjz402j_2Query::getRow($this->field["VIEWCD"]);
//            $this->field["UPDATED"] = $row["UPDATED"];
        } */
        return $result;
    }
    //変数をクリアします
    function clean()
    {
    }
}
?>
