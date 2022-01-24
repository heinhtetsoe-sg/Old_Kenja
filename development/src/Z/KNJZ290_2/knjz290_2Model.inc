<?php

require_once('for_php7.php');

class knjz290_2Model extends Model {
    var $field      = array();
    var $subField2  = array();
    var $cmd;
    var $staffcd;        //primary getで取得
    var $classcd;
    //var $sdate;
    //var $edate;
    var $selectStaff    = array();
    var $selectSubclass = array();
    var $year;

    /**
     * メンバ変数の初期化
     */
    function knjz290_2Model()
    {
    }
    function init()
    {              
        $this->field = array("STAFFCD"              =>  VARS::post("STAFFCD"),
                             "STAFFNAME"            =>  VARS::post("STAFFNAME"),
                             "STAFFNAME_SHOW"       =>  VARS::post("STAFFNAME_SHOW"),
                             "STAFFNAME_KANA"       =>  VARS::post("STAFFNAME_KANA"),
                             "STAFFNAME_ENG"        =>  VARS::post("STAFFNAME_ENG"),
                             "STAFFNAME_REAL"       =>  VARS::post("STAFFNAME_REAL"),
                             "STAFFNAME_KANA_REAL"  =>  VARS::post("STAFFNAME_KANA_REAL"),
                             "JOBNAME"              =>  VARS::post("JOBNAME"),
                             "SECTIONABBV"          =>  VARS::post("SECTIONABBV"),
                             "SHARENAME"            =>  VARS::post("SHARENAME"),
                             "SHARENAME2"           =>  VARS::post("SHARENAME2"),
                             "CHARGECLASS"          =>  VARS::post("CHARGECLASS"),
                             "NAME2"                =>  VARS::post("NAME2"),
                             "STAFFBIRTHDAY"        =>  VARS::post("STAFFBIRTHDAY"),
                             "STAFFZIPCD"           =>  VARS::post("STAFFZIPCD"),
                             "STAFFADDR1"           =>  VARS::post("STAFFADDR1"),
                             "STAFFADDR2"           =>  VARS::post("STAFFADDR2"),
                             "STAFFTELNO"           =>  VARS::post("STAFFTELNO"),
                             "STAFFFAXNO"           =>  VARS::post("STAFFFAXNO"),
                             "STAFFE_MAIL"          =>  VARS::post("STAFFE_MAIL"),
                             "POSITIONCD1"          =>  VARS::post("POSITIONCD1"),
                             "POSITIONCD2"          =>  VARS::post("POSITIONCD2"),
                             "POSITIONCD3"          =>  VARS::post("POSITIONCD3"),
                             "POSITIONCD1_MANAGER"  =>  VARS::post("POSITIONCD1_MANAGER"),
                             "POSITIONCD2_MANAGER"  =>  VARS::post("POSITIONCD2_MANAGER"),
                             "POSITIONCD3_MANAGER"  =>  VARS::post("POSITIONCD3_MANAGER"),
                             "CHARGENURSEOFF"       =>  VARS::post("CHARGENURSEOFF"),
                             "UPDATED"              =>  VARS::post("UPDATED"),
                             );
                             
        $this->subField2 = array("STAFFCD"              =>  VARS::post("STAFFCD"),
                                 "CLASSCD"              =>  VARS::post("CLASSCD"),
                                 "SDATE"                =>  str_replace("/","-",VARS::post("SDATE")),
                                 "EDATE"                =>  str_replace("/","-",VARS::post("EDATE"))
                                 );

        //プロパティファイル
        $this->getPropertiesAll();
        
        $this->cmd = VARS::request("cmd");

        if ($this->cmd == "subform1") {
            $this->subField2 = array();
            unset($this->classcd);
            unset($this->sdate);
            //教育課程用
            unset($this->school_kind);
            unset($this->curriculum_cd);
        }
        
        if (VARS::get("mode") == "1"){
            unset($this->staffcd);
        }
        
        $temp = VARS::get("STAFFCD");
        if (isset($temp)){
            $this->staffcd  = VARS::get("STAFFCD");
        }

        $temp2 = VARS::get("CLASSCD");
        if (isset($temp2)){
            $this->classcd  = VARS::get("CLASSCD");
            //教育課程用
            $this->school_kind    = VARS::get("SCHOOL_KIND");
            $this->curriculum_cd  = VARS::get("CURRICULUM_CD");
        }

        $temp3 = VARS::get("SDATE");
        if (isset($temp3)){
            $this->sdate  = VARS::get("SDATE");
        }
        
        $temp4 = VARS::get("EDATE");
        if (isset($temp3)){
            $this->edate  = VARS::get("EDATE");
        }
                    
        $this->selectSubclass = array();
        $this->selectSubclass = explode(",", VARS::post("selectSubclass"));

        $this->selectStaff = array();
        $this->selectStaff = explode(",", VARS::post("selectStaff"));
        
        if ($this->cmd == ""){
            unset($this->sdate);
            unset($this->year);
        }
        
        //年度（初期値）
        $this->year = ($this->year == "") ? CTRL_YEAR : $this->year;
        if (VARS::post("YEAR")){
            $this->year = VARS::post("YEAR");
        }
    }

   //入力項目チェック
    function validateInput()
    {
        $setsize = "";
        $zero_ume = "";
        //STAFFCDフィールドサイズ変更対応
        if ($this->Properties["useStaffcdFieldSize"] === '10') {
            $setsize = 10;
            $zero_ume = "%010d";
        } else {
            $setsize = 8;
            $zero_ume = "%08d";
        }
    
        //キーのチェック
        if (trim($this->field["STAFFCD"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901',"(職員コード)");
            return false;
        }
        if (trim($this->field["STAFFNAME"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901',"(職員氏名)");
            return false;
        }
        if (trim($this->field["STAFFNAME_SHOW"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901',"(職員氏名表示用)");
            return false;
        }
        if ($this->staffcd  == "" && $this->cmd == "update") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->cmd == "update" && $this->staffcd != $this->field["STAFFCD"]) {
            $this->setWarning('MSG308');
            return false;
        }
        //コードのゼロ埋め
        if (strlen($this->field["STAFFCD"]) < $setsize){
            $this->field["STAFFCD"] = sprintf("$zero_ume", $this->field["STAFFCD"]);
        }

        //１レコード取得
        $row = knjz290_2Query::getRow($this, $this->field["STAFFCD"]);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning('MSG303');
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning('MSG918', "(職員コード)");
            return false;
        }
        //肩書き
        if ($this->cmd == "update" || $this->cmd == "add") {
            if ($this->field["POSITIONCD1"] != "" && $this->field["POSITIONCD2"] != "" && $this->field["POSITIONCD1"] == $this->field["POSITIONCD2"]) {
                if ($this->field["POSITIONCD1"] == $this->field["POSITIONCD3"]) {
                    $this->setWarning('MSG302', "(肩書き)");
                    return false;
                } 
                if ($this->field["POSITIONCD1"] != "1050") {
                    $this->setWarning('MSG302', "(肩書き)");
                    return false;
                }
            }
            if ($this->field["POSITIONCD2"] != "" && $this->field["POSITIONCD3"] != "" && $this->field["POSITIONCD2"] == $this->field["POSITIONCD3"]) {
                if ($this->field["POSITIONCD2"] == $this->field["POSITIONCD1"]) {
                    $this->setWarning('MSG302', "(肩書き)");
                    return false;
                } 
                if ($this->field["POSITIONCD2"] != "1050") {
                    $this->setWarning('MSG302', "(肩書き)");
                    return false;
                }
            }
            if ($this->field["POSITIONCD3"] != "" && $this->field["POSITIONCD1"] != "" && $this->field["POSITIONCD3"] == $this->field["POSITIONCD1"]) {
                if ($this->field["POSITIONCD3"] == $this->field["POSITIONCD2"]) {
                    $this->setWarning('MSG302', "(肩書き)");
                    return false;
                } 
                if ($this->field["POSITIONCD3"] != "1050") {
                    $this->setWarning('MSG302', "(肩書き)");
                    return false;
                }
            }
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning('MSG307');
            return false;
        }
        //文字型チェック
        foreach(array("STAFFTELNO"   => array("[^-|0-9]",        "( 電話番号 )"),
                      "STAFFFAXNO"   => array("[^-|0-9]",        "( FAX番号 )")
                ) as $key => $val
        ) {
            if ($this->field[$key] !="" && preg_match('{' . $val[0] . '}i', trim($this->field[$key]))) {
                $this->setWarning("MSG901", $val[1]);
                return false;
            }
        }
        if (strlen($this->field["STAFFCD"]) > $setsize ) {
            $this->setWarning('MSG901', "(職員コード) ".$setsize."バイトまで");
            return false;
        } elseif (strlen($this->field["STAFFNAME"])>60){ 
            $this->setWarning('MSG901', "(職員氏名) 60バイトまで");
            return false;
        } elseif (strlen($this->field["STAFFNAME_SHOW"])>15){ 
            $this->setWarning('MSG901', "(職員氏名表示用) 15バイトまで");
            return false;
        } elseif (strlen($this->field["STAFFNAME_KANA"])>120){ 
            $this->setWarning('MSG901', "(職員氏名カナ) 120バイトまで");
            return false;
        } elseif (strlen($this->field["STAFFNAME_ENG"])>60){ 
            $this->setWarning('MSG901', "(職員氏名英字) 60バイトまで");
            return false;
        } elseif (strlen($this->field["STAFFNAME_REAL"])>120){ 
            $this->setWarning('MSG901', "(戸籍氏名) 120バイトまで");
            return false;
        } elseif (strlen($this->field["STAFFNAME_KANA_REAL"])>240){ 
            $this->setWarning('MSG901', "(戸籍氏名かな) 240バイトまで");
            return false;
        } elseif (strlen($this->field["STAFFZIPCD"])>8){ 
            $this->setWarning('MSG901', "(郵便番号) 8バイトまで");
            return false;
        } elseif (strlen($this->field["STAFFADDR1"])>90){ 
            $this->setWarning('MSG901', "(住所1) 90バイトまで");
            return false;
        } elseif (strlen($this->field["STAFFADDR2"])>90){ 
            $this->setWarning('MSG901', "(住所2) 90バイトまで");
            return false;
        } elseif (strlen($this->field["STAFFTELNO"])>14){ 
            $this->setWarning('MSG901', "(電話番号) 14バイトまで");
            return false;
        } elseif (strlen($this->field["STAFFFAXNO"])>14){ 
            $this->setWarning('MSG901', "(FAX番号) 14バイトまで");
            return false;
        } elseif (strlen($this->field["STAFFE_MAIL"])>50){
            $this->setWarning('MSG901', "(E-Mailアドレス) 50バイトまで");
            return false;
        }
        return true;
    }

    //入力項目チェック
    //サブフォーム画面のチェック
    function validateInputSubform()
    {
        //キーのチェック
        if (trim($this->staffcd) == "" && ($this->cmd == "subform1_add" || $this->cmd == "subform1_update" || $this->cmd == "subform1_delete")) {
            $this->setWarning('MSG301');
            return false;
        }
        if (trim($this->subField2["CLASSCD"]) == "" && ($this->cmd == "subform1_add" || $this->cmd == "subform1_update" || $this->cmd == "subform1_delete")) {
            $this->setWarning('MSG301',"(教科)");
            return false;
        }
        if (trim($this->subField2["SDATE"]) == "" && ($this->cmd == "subform1_add" || $this->cmd == "subform1_update" || $this->cmd == "subform1_delete")) {
            $this->setWarning('MSG301',"(開始日付)");
            return false;
        }
        if (trim($this->subField2["EDATE"]) == "" && ($this->cmd == "subform1_add" || $this->cmd == "subform1_update" || $this->cmd == "subform1_delete")) {
            $this->setWarning('MSG301',"(終了日付)");
            return false;
        }
        
        //更新,削除時にリストから選択しているかをチェック
        if ($this->sdate  == "" && ($this->cmd == "subform1_update" || $this->cmd == "subform1_delete")) {
            $this->setWarning('MSG308');
            return false;
        }
        
        //開始日付と終了日付の日付の前後関係をチェック
        if ($this->cmd =="subform1_add" || $this->cmd =="subform1_update") {
            $SDATE  = date("Y-m-d",strtotime(str_replace("-","/",$this->subField2["SDATE"])));
            $EDATE  = date("Y-m-d",strtotime(str_replace("-","/",$this->subField2["EDATE"])));
            if ($EDATE <= $SDATE) {
                $this->setWarning("MSG916","( 有効期間 )");
                return false;
            }
        }
        
        //重複教科エラーチェック
        $db = Query::dbCheckOut();
        if ($this->cmd =="subform1_add") {
            $query = knjz290_2Query::get_cnt_Classcd($this->staffcd,$this->subField2["CLASSCD"], $this);
            $cnt_G_Address = $db->getOne($query);
            if (($this->cmd =="subform1_add") && $cnt_G_Address > 0) {
                $this->setWarning("MSG302","( 同一の教科 )");
                return false;
            }
        }
        Query::dbCheckIn($db);

        //重複エラーチェック
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $Row = knjz290_2Query::getRow2check($this->staffcd,$this->subField2["CLASSCD"],$this->subField2["SDATE"], $this);
        } else {
            $Row = knjz290_2Query::getRow2($this->staffcd,$this->subField2["CLASSCD"],$this->subField2["SDATE"], $this);
        }

        if ($this->cmd == "subform1_add" && is_array($Row)){
            $this->setWarning('MSG302');
            return false;
        }
        return true;
    }
    
    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage('MSG201');
        }    
    }
    
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->staffcd = $this->field["STAFFCD"];
            $this->setMessage('MSG201');
            $this->year = "ALL";
        }
    }

    /**
     * マスタメンテ用 編集完了用のモデルを返す
    */
    function &getUpdateSubformModel()
    {
        //入力チェック
        if ($this->validateInputSubform()){
            $this->update2();
            $this->setMessage('MSG201');
        }    
    }

    
    function &getInsertSubformModel()
    {
        //入力チェック
        if ($this->validateInputSubform()){
            $this->insert2();
            //$this->staffcd = $this->subField2["STAFFCD"];
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $class_array = array();
                $class_array = explode("-", $this->subField2["CLASSCD"]);
                $this->classcd = $class_array[0];
                $this->school_kind = $class_array[1];
                $this->curriculum_cd = $class_array[2];
            } else {
                $this->classcd = $this->subField2["CLASSCD"];
            }
            $this->sdate = $this->subField2["SDATE"];
            $this->setMessage('MSG201');
        }
    }
    
    function &getDeleteSubformModel()
    {
        //入力チェック
        if ($this->validateInputSubform()){
            $this->delete2();
            $this->setMessage('MSG201');
        }
    }

    //マスタメンテここまで--------------------------------


    // 削除用のモデルを返します
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->staffcd == "") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->staffcd != $this->field["STAFFCD"]) {
            $this->setWarning('MSG308');
            return;
        }
        $row = knjz290_2Query::getRow($this, $this->staffcd);
        if (!is_array($row)){
            $this->setWarning('MSG303');
            return;
        }
        if (knjz290_2Query::IsExisting($this->staffcd)){
            $this->setWarning('MSG919',"年度データに登録されています。");
            return;
        }
        knjz290_2Query::getDeleteQuery($this, $this->field);
        $this->setMessage('MSG201');
    }

    function &insert()
    {
        knjz290_2Query::getInsertQuery($this->field);
        knjz290_2Query::getUpdateDetailQuery($this, $this->field);
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjz290_2Query::getUpdateQuery($this->field);
        knjz290_2Query::getUpdateDetailQuery($this, $this->field);
        knjz290_2Query::newDataUpdate($this, $this->field);
    }
    
    //マスタメンテ用の内部メソッド--------------------------------------
    function &insert2()
    {
        knjz290_2Query::getInsert2Query($this->subField2, $this);
    }

    function &update2()
    {
        $result = knjz290_2Query::getUpdate2Query($this);
        if ( !isset($this->warning)){
            $this->setMessage("MSG201");
        }
        $this->sdate = $this->subField2["SDATE"];
        return $result;
    }
    
    function &delete2()
    {
        $result = knjz290_2Query::getDelete2Query($this);
        if ( !isset($this->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }
    //マスタメンテ用の内部メソッドここまで-------------------------------

    //資格科目登録の更新
    function &getUpdateSubModel1()
    {
        //入力チェック
        if ($this->staffcd){
            knjz290_2Query::getUpdateSubQuery1($this);
            $this->setMessage('MSG201');
        }    
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
    
    /**
     * 前年度コピー(職員詳細データ)のモデルを返します
    */
    function &getCopyModel()
    {
        $result = knjz290_2Query::getCopyDetail($this);
        if ( !isset($this->warning)){
            $this->setMessage("MSG201");
        }
    }
}
?>
