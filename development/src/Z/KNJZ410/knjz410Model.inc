<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjz410Model.inc 56591 2017-10-22 13:04:39Z maeshiro $
//定数定義
class knjz410Model extends Model {
    var $field      = array();
    var $cmd;
    var $school_cd;
    //メンバ変数の初期化
    function knjz410Model()
    {
    }
    function init()
    {
		$this->field = array("SCHOOL_CD"		  =>  VARS::post("SCHOOL_CD"),
							 "SCHOOL_NAME"		  =>  VARS::post("SCHOOL_NAME"),
							 "BUNAME"			  =>  VARS::post("BUNAME"),
							 "KANAME"			  =>  VARS::post("KANAME"),
							 "SCHOOL_SORT"		  =>  VARS::post("SCHOOL_SORT"),
							 "BUNYA"			  =>  VARS::post("BUNYA"),
							 "AREA_NAME"		  =>  VARS::post("AREA_NAME"),
							 "ZIPCD"			  =>  VARS::post("ZIPCD"),
							 "ADDR1"			  =>  VARS::post("ADDR1"),
							 "ADDR2"			  =>  VARS::post("ADDR2"),
							 "TELNO"			  =>  VARS::post("TELNO"),
							 "GREDES"			  =>  VARS::post("GREDES"),
							 );

        $this->cmd             = VARS::request("cmd");
       if (VARS::get("PROGRAMID")) {
           $this->school_cd = "";
       }
        if (VARS::get("SCHOOL_CD")!="") {
            $this->school_cd    = VARS::get("SCHOOL_CD");
        }
    }
    //入力項目チェック
    function validateInput($kind)
    {
        if (trim($this->field["SCHOOL_CD"]) == ""){
            $this->setWarning("MSG301","(学校コード)");
            return false;
        }

        if ($kind=="update" || $kind=="delete"){
            //リストより選択していない
            if ($this->school_cd==""){
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["SCHOOL_CD"] !=$this->school_cd){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }

            //コードのゼロ埋め
            $this->field["SCHOOL_CD"] = sprintf("%08d",$this->field["SCHOOL_CD"]);
            //１レコード取得
            $row = knjz410Query::getRow($this->field["SCHOOL_CD"]);

            //存在チェック
            if (!is_array($row)){
                $this->setWarning("MSG303");
                return false;
            }
        }
        if ($kind=="update" || $kind=="insert"){

            //コードのゼロ埋め
            $this->field["SCHOOL_CD"] = sprintf("%08d",$this->field["SCHOOL_CD"]);
            //１レコード取得
            $row = knjz410Query::getRow($this->field["SCHOOL_CD"]);

            //重複チェック
            if ($this->cmd == "add" && is_array($row)){
                $this->setWarning("MSG918","(学校コード)");
                return false;
            }
            if (strlen($this->field["SCHOOL_NAME"]) > 120){
                $this->setWarning("MSG915","(学校名称は120バイトまでです。)");
                return false;
            } elseif (strlen($this->field["BUNAME"]) > 120){ 
                $this->setWarning("MSG915","(学部名は120バイトまでです。)");
                return false;
            } elseif (strlen($this->field["KANAME"]) > 120){ 
                $this->setWarning("MSG915","(学科名は120バイトまでです。)");
                return false;
            } elseif (strlen($this->field["KANAME"]) > 120){ 
                $this->setWarning("MSG915","(学科名は120バイトまでです。)");
                return false;
            } elseif (strlen($this->field["AREA_NAME"]) > 30){ 
                $this->setWarning("MSG915","(所在地は30バイトまでです。)");
                return false;
            } elseif (strlen($this->field["ZIPCD"]) > 8){ 
                $this->setWarning("MSG915","(郵便番号は8バイトまでです。)");
                return false;
            } elseif (strlen($this->field["ADDR1"]) > 90){ 
                $this->setWarning("MSG915","(住所１は90バイトまでです。)");
                return false;
            } elseif (strlen($this->field["ADDR2"]) > 90){ 
                $this->setWarning("MSG915","(住所２は90バイトまでです。)");
                return false;
            } elseif (strlen($this->field["TELNO"]) > 16){ 
                $this->setWarning("MSG915","(電話番号は16バイトまでです。)");
                return false;
            } elseif (strlen($this->field["GREDES"]) > 120){ 
                $this->setWarning("MSG915","(評定基準は120バイトまでです。)");
                return false;
            }

        }
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
            $this->school_cd = $this->field["SCHOOL_CD"];
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
        if (!isset($this->school_cd)) {
            $this->setWarning("MSG303");
            return;
        }
        $result = knjz410Query::getDeleteQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }
    function &insert()
    {
        $result = knjz410Query::getInsertQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz410Query::getUpdateQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }
    //変数をクリアします
    function clean()
    {
    }
}
?>
