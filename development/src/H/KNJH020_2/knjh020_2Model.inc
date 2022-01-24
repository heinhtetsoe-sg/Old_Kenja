<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}
class knjh020_2Model extends Model {
    var $cmd;
    var $field = array();
	var $prg;
	var $auth;				//NO001
    var $schregno;			//学籍番号
    var $bros_schregno;		//兄弟姉妹学籍番号
    var $relano;			//連番

    /**
     * メンバ変数の初期化
     */
    function knjh020_2Model()
    {
			unset($this->auth);	//NO001
    }
    
    function init()
    {   
        //生徒親族情報
        $this->field = array("RELANAME"         =>  VARS::post("RELANAME"),         //生徒親族氏名
                             "RELAKANA"         =>  VARS::post("RELAKANA"),         //生徒親族かな
                             "RELASEX"          =>  VARS::post("RELASEX"),          //性別
                             "RELABIRTHDAY"     =>  VARS::post("RELABIRTHDAY"),     //誕生日
                             "RELATIONSHIP"     =>  VARS::post("RELATIONSHIP"),     //続柄
                             "OCCUPATION"       =>  VARS::post("OCCUPATION"),       //職業または学校
                             "RELA_SCHREGNO"    =>  VARS::post("RELA_SCHREGNO"),    //親族学籍番号
                             "REGIDENTIALCD"    =>  VARS::post("REGIDENTIALCD"),    //同居区分
                             "REMARK"           =>  VARS::post("REMARK"),           //備考
                             "UPDATED"          =>  VARS::post("UPDATED")           //更新日付
                        );

        $this->cmd         = VARS::request("cmd");

        if(VARS::request("SCHREGNO")){
            $this->schregno = VARS::request("SCHREGNO");
            unset($this->relano);
            unset($this->rela_schregno);
        }

        if(VARS::request("RELANO")){
            $this->relano     = VARS::request("RELANO");
        }
        if(VARS::request("RELA_SCHREGNO")){
            $this->rela_schregno = VARS::request("RELA_SCHREGNO");
        }

		//NO001
		if(VARS::get("AUTH")) {
			$this->auth = VARS::get("AUTH");
		}

        //兄弟データの元の学籍番号
        $this->stucd = VARS::request("STUCD");
        //追加    
        if(VARS::get("NEWAD")){
            unset($this->relano);
            unset($this->rela_schregno);
        }
    }
    
    //入力項目チェック
    function validateInput($type)
    {
		//KNJH160からのコール
		if(VARS::get("PRG") == "KNJH160") {
			$this->prg = VARS::get("PRG");
		}
		//NO001
        if ($this->auth && $this->auth < DEF_UPDATE_RESTRICT){
            $this->setWarning("MSG300");
            return false;
        }
        if(!$this->schregno){
            $this->setWarning("MSG304");
            return false;
        }

        if($type == "update" || $type == "delete"){
            if(!$this->relano){
                $this->setWarning("MSG303");
                return false;
            }
/*
            //１レコード取得
            $row = knjh020_2Query::getRow_relative($this);
            if($row["UPDATED"] != $this->field["UPDATED"]){
                $this->setWarning("MSG307 ");
                return false;
            }
*/
        }
        
        //文字数チェック
        if($type == "update" || $type == "insert"){
            if (strlen($this->field["RELANAME"]) > 60) {
                $this->setWarning("MSG901", "(親族氏名) 60バイトまで");
                return false;
            } elseif (strlen($this->field["RELAKANA"]) > 120 ) {
                $this->setWarning("MSG901", "(親族氏名かな) 120バイトまで");
                return false;
            } elseif (strlen($this->field["OCCUPATION"]) > 60 ) {
                $this->setWarning("MSG901", "(職業又は学校) 60バイトまで");
                return false;
            } elseif (strlen($this->field["REMARK"]) > 45 ) {
                $this->setWarning("MSG901", "(備考) 45バイトまで");
                return false;
            } elseif ($this->field["RELANAME"] == "") {
                $this->setWarning("MSG901", "(親族氏名)");
                return false;
            } elseif ($this->field["RELAKANA"] == "") {
                $this->setWarning("MSG901", "(親族氏名かな)");
                return false;
            }
        }
        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            if($this->update()){
                $this->setMessage("MSG201");
            }
        }
    }

    //insert処理
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            if($this->insert()){
                $this->setMessage("MSG201");
            }
        }
    }

    //delete処理
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            if($this->delete()){
                $this->setMessage("MSG201");
            }
        }
    }
    //反映処理
    function &getApplyModel()
    {
        if (knjh020_2Query::add_Schreg_Rela_Dat($this)){
            $this->setMessage("MSG201");
        }else{
            $this->setWarning("MSG303");
        }
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        //得点入力画面編集対象データの書込み処理
        $result = knjh020_2Query::getUpdateQuery($this);
        return $result;
    }

    /**
     * インサート時の内部メソッド
     */
    function &insert()
    {
        //得点入力画面編集対象データの書込み処理
        $rela_max = knjh020_2Query::getRela_Maxno($this);
        $relano = (($rela_max=="")? 1 : ($rela_max+1));
        $result = knjh020_2Query::getInsertQuery($this,$relano);
        if($result){
            $this->relano = $relano;
        }
        return $result;
    }

    /**
     * デリート時の内部メソッド
     */
    function &delete()
    {
        //得点入力画面編集対象データの書込み処理
        $result = knjh020_2Query::getDeleteQuery($this);
        if($result){
            $this->cleaner();
        }
        return $result;
    }

    /**
     * 変数を初期化します
     */
    function cleaner()
    {
        $this->relano = "";
        $this->rela_schregno = "";
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

}
?>
