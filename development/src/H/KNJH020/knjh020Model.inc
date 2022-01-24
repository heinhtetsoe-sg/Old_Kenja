<?php

require_once('for_php7.php');

/**
 * 定数定義
 */

class knjh020Model extends Model {
    //startknjh020
    var $field      = array();
    var $cmd;
    var $GradeClass;
    var $schregno;
    var $addno;
	var $prg;
	var $auth;				//NO001
    var $clear;
    var $control_data = array();    //コントロールマスタデータを保持

    /**
     * メンバ変数の初期化
     */
    function knjh020Model()
    {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
		unset($this->auth);
    }
    function init()
    {
        //生徒保護者情報
        $this->field["GUARD_NAME"]          =  VARS::post("GUARD_NAME");            //保護者氏名
        $this->field["GUARD_KANA"]          =  VARS::post("GUARD_KANA");            //かな
        $this->field["GUARD_SEX"]           =  VARS::post("GUARD_SEX");             //性別
        $this->field["GUARD_BIRTHDAY"]      =  VARS::post("GUARD_BIRTHDAY");        //誕生日
        $this->field["RELATIONSHIP"]        =  VARS::post("RELATIONSHIP");          //続柄
        $this->field["GUARD_ZIPCD"]         =  VARS::post("J_ZIPCD");               //郵便番号
        $this->field["GUARD_ADDR1"]         =  VARS::post("GUARD_ADDR1");           //住所１
        $this->field["GUARD_ADDR2"]         =  VARS::post("GUARD_ADDR2");           //住所２
        $this->field["GUARD_TELNO"]         =  VARS::post("GUARD_TELNO");           //電話番号
        $this->field["GUARD_FAXNO"]         =  VARS::post("GUARD_FAXNO");           //ＦＡＸ番号
        $this->field["GUARD_E_MAIL"]        =  VARS::post("GUARD_E_MAIL");          //E-mailアドレス
        $this->field["GUARD_JOBCD"]         =  VARS::post("GUARD_JOBCD");           //職種コード
        $this->field["GUARD_WORK_NAME"]     =  VARS::post("GUARD_WORK_NAME");       //勤務先名称
        $this->field["GUARD_WORK_TELNO"]    =  VARS::post("GUARD_WORK_TELNO");      //勤務先電話番号
        $this->field["UPDATED"]             =  VARS::post("UPDATED");               //更新日付
        //生徒保証人情報
        $this->field["GUARANTOR_NAME"]          =  VARS::post("GUARANTOR_NAME");            //氏名
        $this->field["GUARANTOR_KANA"]          =  VARS::post("GUARANTOR_KANA");            //かな
        $this->field["GUARANTOR_SEX"]           =  VARS::post("GUARANTOR_SEX");             //性別
        $this->field["GUARANTOR_RELATIONSHIP"]  =  VARS::post("GUARANTOR_RELATIONSHIP");    //続柄
        $this->field["GUARANTOR_ZIPCD"]         =  VARS::post("J_GUARANTOR_ZIPCD");         //郵便番号
        $this->field["GUARANTOR_ADDR1"]         =  VARS::post("GUARANTOR_ADDR1");           //住所１
        $this->field["GUARANTOR_ADDR2"]         =  VARS::post("GUARANTOR_ADDR2");           //住所２
        $this->field["GUARANTOR_TELNO"]         =  VARS::post("GUARANTOR_TELNO");           //電話番号
        $this->field["GUARANTOR_JOBCD"]         =  VARS::post("GUARANTOR_JOBCD");           //職種コード
        $this->field["PUBLIC_OFFICE"]           =  VARS::post("PUBLIC_OFFICE");             //兼ねている公職


        $this->cmd      = VARS::request("cmd");
        $post_year      = VARS::request("YEAR");
        $this->year     = VARS::post("year");
        $this->clear    = VARS::post("clear");

        $this->addno    = VARS::post("J_STUCD");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if(VARS::post("GrCl")){
            $this->GradeClass = VARS::post("GrCl");
        }
        if(VARS::get("init") == 1){
            $this->schregno = "";
        }else if(VARS::request("SCHREGNO")){
            $this->schregno = VARS::request("SCHREGNO");
        }
        if (VARS::get("PROGRAMID")) {
            unset($this->schregno);
			unset($this->auth);	//NO001
        }
		//KNJH160からのコール
		if(VARS::get("PRG")) {
			$this->prg = VARS::get("PRG");
		}
		//NO001
		if(VARS::get("AUTH")) {
			$this->auth = VARS::get("AUTH");
		}
    }

    //入力項目チェック
    function validateInput($type)
    {
		//NO001
        if ($this->auth && $this->auth < DEF_UPDATE_RESTRICT){
            $this->setWarning("MSG300");
            return false;
        }

        if(!$this->schregno){
            $this->setWarning("MSG308");
            return false;
        }

        $db = Query::dbCheckOut();
        if($type == "add"){

            $query = knjh020Query::getParentsdata_cnt($this->addno);
            $row = $db->getOne($query);
            if($row != 0){
                $this->setWarning("MSG104","学籍保護者データ");
                return false;
            }
        }

        if($type == "delete"){
            $query = knjh020Query::getGuardian($this->schregno);
            $row = $db->getOne($query);

            if($row == 0){
                $this->setWarning("MSG303");
                return false;
            }
        }
        Query::dbCheckIn($db);
/*
        if($type == "update" || $type == "delete"){
            //１レコード取得
			$row = knjh020Query::getRow_parents($this);

			if($row["UPDATED"] != $this->field["UPDATED"]){
				$this->setWarning("MSG307");
				return false;
			}
        }
*/
        //文字数チェック
        if($type == "update"){
            if (strlen($this->field["GUARD_NAME"]) > 60 ) {
                $this->setWarning("MSG901", "(保護者氏名漢字) 60バイトまで");
                return false;
            } elseif (strlen($this->field["GUARD_KANA"]) > 120 ) {
                $this->setWarning("MSG901", "(保護者氏名かな) 120バイトまで");
                return false;
            } elseif (strlen($this->field["GUARD_ADDR1"]) > 90 ) {
                $this->setWarning("MSG901", "(保護者住所1) 90バイトまで");
                return false;
            } elseif (strlen($this->field["GUARD_ADDR2"]) > 90 ) {
                $this->setWarning("MSG901", "(保護者住所2) 90バイトまで");
                return false;
            } elseif (strlen($this->field["GUARD_TELNO"]) > 14 ) {
                $this->setWarning("MSG901", "(電話番号) 14バイトまで");
                return false;
            } elseif (strlen($this->field["GUARD_FAXNO"]) > 14 ) {
                $this->setWarning("MSG901", "(FAX番号) 14バイトまで");
                return false;
            } elseif (strlen($this->field["E_MAIL"]) > 20 ) {
                $this->setWarning("MSG901", "(GUARD_E_MAIL) 20バイトまで");
                return false;
            } elseif (strlen($this->field["GUARD_WORK_NAME"]) > 60 ) {
                $this->setWarning("MSG901", "(勤務先名称) 60バイトまで");
                return false;
            } elseif (strlen($this->field["GUARD_WORK_TELNO"]) > 14 ) {
                $this->setWarning("MSG901", "(勤務先電話番号) 14バイトまで");
                return false;
            } elseif ($this->field["RELATIONSHIP"] == "00") {
                $this->setWarning("MSG901", "(続柄)");
                return false;
            }

            if (strlen($this->field["GUARANTOR_NAME"]) > 60 ) {
                $this->setWarning("MSG901", "(保証人氏名漢字) 60バイトまで");
                return false;
            } elseif (strlen($this->field["GUARANTOR_KANA"]) > 120 ) {
                $this->setWarning("MSG901", "(保証人氏名かな) 120バイトまで");
                return false;
            } elseif (strlen($this->field["GUARANTOR_ADDR1"]) > 90 ) {
                $this->setWarning("MSG901", "(保証人住所1) 90バイトまで");
                return false;
            } elseif (strlen($this->field["GUARANTOR_ADDR2"]) > 90 ) {
                $this->setWarning("MSG901", "(保証人住所2) 90バイトまで");
                return false;
            } elseif (strlen($this->field["GUARANTOR_TELNO"]) > 14 ) {
                $this->setWarning("MSG901", "(電話番号) 14バイトまで");
                return false;
            } elseif (strlen($this->field["PUBLIC_OFFICE"]) > 30 ) {
                $this->setWarning("MSG901", "(兼ねている公職) 30バイトまで");
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
        if ($this->validateInput("update"))
        {
            if($this->insert()){
                $this->setMessage("MSG201");
                return;
            }
        }
    }

    function &getAddingModel()
    {
        //入力チェック
        if ($this->validateInput("add")){
            if($this->reflect()){
                if($this->add()){
                    $this->setMessage("MSG201");
                }
            }
        }
    }

    /**
     * 削除用のモデルを返します
     */
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            $this->delete();
            $this->setMessage("MSG201");
        }
    }

    function &delete()
    {
        $result = knjh020Query::getDeleteQuery($this->schregno);
        return $result;
    }

    function &insert()
    {
        $result = knjh020Query::getInsertQuery($this);
        return $result;
    }

    //兄弟姉妹データからコピー
    function &reflect()
    {
        $reflect = knjh020Query::getRow_parents($this->addno);
        return $reflect;
    }
     function &add()
     {
         $result = knjh020Query::getAddingQuery($this);
         return $result;
     }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
