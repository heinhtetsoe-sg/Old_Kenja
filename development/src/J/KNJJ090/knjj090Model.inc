<?php

require_once('for_php7.php');

class knjj090Model extends Model {

	var $field		= array();
	var $cmd;
	var $schregno;					//学籍番号
	var $index;						//連番
	var $control_data = array();	//コントロールマスタデータを保持
	var $sec_competence;			//セキュリティーチェック
	var $grade;						//学年
    var $org_data;      			//変更前のデータ

	function knjj090Model()
	{
		common::GetControlMaster_Fnc($this->control_data); 
		unset($this->grade);	//2004/05/12 nakamoto add
	}
	function init()
	{
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

		$this->field = array("COMMITTEECD" 	=>  VARS::post("COMMITTEECD"),		//委員会
							 "EXECUTIVECD"	=>  VARS::post("EXECUTIVECD"),		//役職
							 "CHARGENAME"	=>  VARS::post("CHARGENAME"),		//係り名
							 "SEQPOST"	 	=>  VARS::post("SEQPOST"),			//連番
							 "temp_cd"		=>  VARS::post("temp_cd"),			//最初のCD
							 "UPDATED"		=>  VARS::post("UPDATED")			//更新日付
							 );

		//list→right_list,right_list→edit
		$this->schregno			= VARS::request("SCHREGNO");
		if (VARS::request("SCHREGNO")=="")			$this->schregno			= VARS::post("SCHREGNO");

		//right_list→edit
//		$this->index			= VARS::get("SEQ");/* NO003 */

		$this->index			= VARS::request("SEQ");/* NO003 */
        if (VARS::request("SEQ")=="") 	$this->index    = VARS::post("SEQPOST");/* NO003 */

		$this->cmd		 = VARS::request("cmd");

		if(VARS::request("GRADE")) {
			$this->grade 	= VARS::request("GRADE");
		} 
		
		//セキュリティーチェック
		$this->sec_competence = common::SecurityCheck(STAFFCD,"KNJJ090");
	}
	//入力項目チェック
	function validateInput($kind)
	{
		// 権限チェック
		if($this->sec_competence != DEF_UPDATABLE && $this->sec_competence != DEF_UPDATE_RESTRICT){
			$this->setWarning("MSG300");
			return false;
		}

		//委員会と係り名どちらか一つは必ず入力する
		if ($kind == "insert") { /* NO002 */
			if($this->field["COMMITTEECD"] == "" && $this->field["CHARGENAME"] == "")
			{
				$this->setWarning("MSG301");				//必須項目が入力されていません
				return false;
			}
		} /* NO002 */

		if ($this->schregno=="") {
			$this->setWarning("MSG304","(左より生徒を選択してから行ってください)");
			return false;
		}

		//$row = knjj090Query::getRow($this,$this->field["SEQPOST"]);	/* NO004move1 */
		$this->isload = "0";

		if ($kind=="update" || $kind=="delete")			//更新・削除のとき
		{
			//リストより選択していない
			if ($this->field["SEQPOST"]==""){
				$this->setWarning("MSG308");
				return false;
			}
			$row = knjj090Query::getRow($this,$this->field["SEQPOST"]);	/* NO004move1 */
			/* NO004↓ */
	        //キー値が変更されていますかをチェック
    	    if ($row["SEQ"] !=$this->field["SEQPOST"]){
        	    $this->isload = "1";
	            $this->setWarning("MSG308");
    	        return false;
        	}
			/* NO004↑ */

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

            //記録備考データ存在チェック
			$cnt = knjj090Query::checkDetailExist($this);
			if ($this->cmd == "delete" && $cnt > 0){
                $this->setWarning("MSG919","（記録備考データ有）");
				return false;
            }
		}
        if ($kind=="update" || $kind=="insert") {
			//文字数チェック
            if (strlen($this->field["CHARGENAME"]) >30 ) {
                $this->setWarning("MSG914","(係り名は30バイトまでです)");
                return false;  
            }
        }
		return true;
	}

//	/**
//	 * 編集完了用のモデルを返します
//	*/


//	/**
//	 * 更新用のモデルを返します
//	*/
	function &getUpdateModel()
	{
		//入力チェック
		if ($this->validateInput("update")){
			$this->update();
		} else {
			$this->isload = "1";
		}
	}
	function &update()
	{
		$result = knjj090Query::getUpdateQuery($this);
		if ( !isset($model->warning)){
			$this->setMessage("MSG201");
		}
		return $result;
	}


//	/**
//	 * 追加用のモデルを返します
//	 */
	function &getInsertModel()
	{
		//入力チェック
		if ($this->validateInput("insert")){
			$this->insert();
		} else {
			$this->isload = "1";
		}
		return;
	}
	function &insert()
	{
	  	$result = knjj090Query::getInsertQuery($this);
		if ( !isset($model->warning)){
			$this->setMessage("MSG201");
			$this->index = $result;	/* NO004 */
		}
		return $result;
	}


//	/**
//	 * 削除用のモデルを返します
//	 */
	function &getDeleteModel()
	{
		//入力チェック
		if ($this->validateInput("delete")){
			$this->delete();
		} else {
			$this->isload = "1";
		}
		return;
	} 
	function &delete()
	{
		if (!isset($this->schregno)) {
			$this->setWarning("MSG308");
			return;
		}
		
		$result = knjj090Query::getDeleteQuery($this);
		
		if ( !isset($model->warning)){
			$this->setMessage("MSG201");
		}
		$this->field = array();
		
		return $result;
	}

}
?>

