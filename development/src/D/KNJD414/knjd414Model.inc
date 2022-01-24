<?php

require_once('for_php7.php');

class knjd414Model extends Model {
    var $cmd;
    var $ctrl;
    var $year;
    var $school_kind = "";
    var $classcd = "";
    var $step_cd = "";
    var $learning_content_cd = "";
    var $learning_content = "";
    var $guidance_content_cd = "";
    var $guidance_content = "";
    var $left = "";
    var $classcd_left = "";
    var $step_cd_left = "";
    var $field;
    var $fields;

    function knjd414Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->ctrl);
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();

        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->school_kind);
            unset($this->classcd);
            unset($this->step_cd);
            unset($this->learning_content_cd);
            unset($this->learning_content);
            unset($this->guidance_content_cd);
            unset($this->guidance_content);
            unset($this->left);
            unset($this->classcd_left);
            unset($this->step_cd_left);
            $this->field = array();
            $this->fields = array();
        }
        if (VARS::post("YEAR") != "") {
            $this->year = VARS::request("YEAR");
        }
        if (VARS::post("SCHOOL_KIND") != "") {
            $this->school_kind = VARS::request("SCHOOL_KIND");
        }
        if (VARS::post("CLASSCD") != "") {
            $this->classcd = VARS::request("CLASSCD");
        }
        if (VARS::post("LEARNING_CONTENT_CD") != "") {
            $this->learning_content_cd = VARS::request("LEARNING_CONTENT_CD");
        }
        if (VARS::post("STEP_CD") != "") {
            $this->step_cd = VARS::request("STEP_CD");
        }
        if (VARS::post("LEARNING_CONTENT") != "") {
            $this->learning_content = VARS::request("LEARNING_CONTENT");
        }
        if (VARS::post("GUIDANCE_CONTENT_CD") != "") {
            $this->guidance_content_cd = VARS::post("GUIDANCE_CONTENT_CD");
        }
        if (VARS::post("GUIDANCE_CONTENT") != "") {
            $this->guidance_content = VARS::post("GUIDANCE_CONTENT");
        }
        if (VARS::post("LEFT") != "") {
            $this->left = VARS::post("LEFT");
        }

        if($this->cmd === 'edit2'){
            $this->step_cd = VARS::request("STEP_CD");
            $this->learning_content_cd = VARS::request("LEARNING_CONTENT_CD");
            $this->classcd = VARS::request("CLASSCD");
            $this->left = VARS::request("LEFT");
        }

        if (VARS::post("CLASSCD_LEFT") != "") {
            $this->classcd_left = VARS::request("CLASSCD_LEFT");
        }
        if (VARS::post("STEP_CD_LEFT") != "") {
            $this->step_cd_left = VARS::request("STEP_CD_LEFT");
        }

        $this->field = array("CLASSCD"                   =>  VARS::post("CLASSCD"),                   //教科
                             "STEP_CD"                   =>  VARS::post("STEP_CD"),                   //段階
                             "SCHOOL_KIND"               =>  VARS::post("SCHOOL_KIND"),               //校種種別
                             "LEARNING_CONTENT_CD"       =>  VARS::post("LEARNING_CONTENT_CD"),       //学習内容コード
                             "LEARNING_CONTENT"          =>  VARS::post("LEARNING_CONTENT"),          //学習内容
                             "GUIDANCE_CONTENT_CD"       =>  VARS::post("GUIDANCE_CONTENT_CD"),       //指導内容数
                             "CLASSCD_LEFT"              =>  VARS::post("CLASSCD_LEFT"),              //教科（左画面）
                             "STEP_CD_LEFT"              =>  VARS::post("STEP_CD_LEFT"),              //段階（左画面）
                             "LEFT"                      =>  VARS::post("LEFT"),                      //左画面
                             "CLASSCD_RIGHT"             =>  VARS::post("CLASSCD_RIGHT"),             //左画面選択時の教科
                             "STEP_CD_RIGHT"             =>  VARS::post("STEP_CD_RIGHT"),             //左画面選択時の段階
                             "LEARNING_CONTENT_CD_RIGHT" =>  VARS::post("LEARNING_CONTENT_CD_RIGHT"), //左画面選択時の段階
                            );

        //初期化
        $this->fields = array();
        for ($i = 1; $i <= $this->field["GUIDANCE_CONTENT_CD"]; $i++) {
            $this->fields["GUIDANCE_CONTENT".$i] = VARS::post("GUIDANCE_CONTENT".$i); //指導内容
        }

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput() {
    
        $db = Query::dbCheckOut();
        //必須入力チェック
        if (!$this->field["CLASSCD"]) {
            $this->setWarning("MSG301", "教科");
            return false;
        }
        if (!$this->field["STEP_CD"]) {
            $this->setWarning("MSG301", "段階");
            return false;
        }
        if (!$this->field["LEARNING_CONTENT_CD"]) {
            $this->setWarning("MSG301", "学習内容コード");
            return false;
        }

        if($this->cmd == "update" || $this->cmd == "delete"){
            if($this->field["CLASSCD_RIGHT"] != $this->field["CLASSCD"] || $this->field["STEP_CD_RIGHT"] != $this->field["STEP_CD"] || $this->field["LEARNING_CONTENT_CD_RIGHT"] != $this->field["LEARNING_CONTENT_CD"]){
                //左右画面チェック
                $this->setWarning("MSG901");
                return false;
            }
        }

        if($this->cmd == "add" || $this->cmd == "update"){
            //学習内容未入力時
            $this->field["LEARNING_CONTENT"] = ($this->field["LEARNING_CONTENT"] != "") ? $this->field["LEARNING_CONTENT"] : "―";

            //指導内容未入力時
            for ($i = 1; $i <= $this->field["GUIDANCE_CONTENT_CD"]; $i++) {
                $this->fields["GUIDANCE_CONTENT".$i] = (VARS::post("GUIDANCE_CONTENT".$i) != "") ? VARS::post("GUIDANCE_CONTENT".$i) : "―"; //指導内容
            }
            //必須入力チェック
            if (!$this->field["GUIDANCE_CONTENT_CD"]) {
                $this->setWarning("MSG301", "指導内容数");
                return false;
            }
            //文字数チェック
            if (strlen($this->field["LEARNING_CONTENT_CD"]) > 2 ) {
                $this->setWarning("MSG301", "(学習内容コード) 2バイトまで");
                return false;
            }
            if (strlen($this->field["CLASSGROUP_NAME"]) > 2 ) {
                $this->setWarning("MSG301", "(指導内容数) 2バイトまで");
                return false;
            }
            if (strlen($this->field["LEARNING_CONTENT"]) > 600 ) {
                $this->setWarning("MSG301", "(学習内容) 600バイトまで");
                return false;
            }
            for ($i = 1; $i <= $this->field["GUIDANCE_CONTENT_CD"]; $i++) {
                if (strlen($this->fields["GUIDANCE_CONTENT".$i]) > 600 ) {
                    $this->setWarning("MSG301", "(指導内容) 600バイトまで");
                    return false;
                }
            }
            if($this->cmd == "add"){
                //存在チェック
                $getData = $db->getOne(knjd414Query::getList($this, $this->field["CLASSCD"], $this->field["STEP_CD"], $this->field["LEARNING_CONTENT_CD"]));
                if ($getData != 0) {
                    $this->setWarning("既にデータが存在します。");
                    return false;
                }
            }
            if($this->cmd == "update"){
                //更新チェック
                $getData = $db->getOne(knjd414Query::getList($this, $this->field["CLASSCD"], $this->field["STEP_CD"], $this->field["LEARNING_CONTENT_CD"]));
                if ($getData == 0) {
                    $this->setWarning("MSG303");
                    return false;
                }
            }
        }
        Query::dbCheckIn($db);
        return true;
    }

    //追加処理
    function &getInsertModel() {
        if ($this->validateInput()) {
            knjd414Query::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjd414Query::UpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel(){
        $db = Query::dbCheckOut();
        $pre_year = $this->year - 1;
        $preYear_cnt = $db->getOne(knjd414Query::getCopyData($pre_year, "cnt"));
        //前年度データチェック
        if ($preYear_cnt == 0) {
            $this->setWarning("MSG203", "前年度データがありません。");
            return false;  
        }
        Query::dbCheckIn($db);
        knjd414Query::getCopyQuery($this, $pre_year);
        $this->setMessage("MSG201");
    }

    //削除処理
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjd414Query::DeleteQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
