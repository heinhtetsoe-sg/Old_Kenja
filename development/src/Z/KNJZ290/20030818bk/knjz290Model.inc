<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjz290Model extends Model {
    var $cmd;
    var $year;  //年度
	var $addcd;
	var $addjob;
	var $addsec;
	var $addsha;
	var $addcla;
	var $addhcl;
	var $addscl;
	var $updcd;
	var $updjob;
	var $updsec;
	var $updsha;
	var $updcla;
	var $updhcl;
	var $updscl;
	var $delcd;
	var $deljob;
	var $delsec;
	var $delsha;
	var $delcla;
	var $delhcl;
	var $delscl;

	//前年度からコピー
	var $copy1;
	var $copy2;
	var $copy3;
	var $copy4;

//	/**
//	 * メンバ変数の初期化
//	 */
	function knjz290Model()
	{
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
	}
	function init()
	{	
		$temp = VARS::post("year");    
		if (isset($temp)) {
			$this->year 		 = VARS::post("year");
		}
//		$this->selectdata        = VARS::post("selectdata");
		$this->addcd			= VARS::post("addcd");
		$this->addjob			= VARS::post("addjob");
		$this->addsec			= VARS::post("addsec");
		$this->addsha			= VARS::post("addsha");
		$this->addcla			= VARS::post("addcla");
		$this->addhcl			= VARS::post("addhcl");
		$this->addscl			= VARS::post("addscl");
		$this->updcd			= VARS::post("updcd");
		$this->updjob			= VARS::post("updjob");
		$this->updsec			= VARS::post("updsec");
		$this->updsha			= VARS::post("updsha");
		$this->updcla			= VARS::post("updcla");
		$this->updhcl			= VARS::post("updhcl");
		$this->updscl			= VARS::post("updscl");
		$this->delcd			= VARS::post("delcd");
		$this->deljob			= VARS::post("deljob");
		$this->delsec			= VARS::post("delsec");
		$this->delsha			= VARS::post("delsha");
		$this->delcla			= VARS::post("delcla");
		$this->delhcl			= VARS::post("delhcl");
		$this->delscl			= VARS::post("delscl");
		$this->copy1			= VARS::post("copy1");
		$this->copy2			= VARS::post("copy2");
		$this->copy3			= VARS::post("copy3");
		$this->copy4			= VARS::post("copy4");
		$this->cmd				 = VARS::request("cmd");
	}

	//入力項目チェック
	function validateInput()
	{
		return true;
	}

	//入力項目チェック(前年度のデータコピーの場合)
	function validateCopyAdd()
	{
		$newyear = $this->year + 1;
		$rowc1 = knjz290Query::getNextJob($newyear);
		if (!is_array($rowc1)){
			$this->setWarning("MC0017");
			return;	
		}

		$rowc2 = knjz290Query::getNextSection($newyear);
		if (!is_array($rowc2)){
			$this->setWarning("MC0017");
			return;	
		}
		return true;
	}

//	/**
//	 * 編集完了用のモデルを返します
//	*/
	function &getUpdateModel()
	{
		//入力チェック
		if ($this->validateInput()){
			$this->addupddel();
			$this->setMessage("MZ0046");
		}
	}

//	/**
//	 * 編集完了用のモデルを返します（前年度のデータのコピーの場合）
//	*/
	function &getCopyModel()
	{
		//入力チェック
		if ($this->validateCopyAdd()){
			$this->copyadd();
			$this->setMessage("MZ0046");
		}
	}


//	/**
//	 * レコード追加＆更新＆削除の内部メソッド
//	 */
	function &addupddel()
	{
		$result = knjz290Query::getAddUpdDelQuery($this);
		return $result;
	}

//	/**
//	 * 前年度からコピー
//	 */
	function &copyadd()
	{
		$result = knjz290Query::getCopyQuery($this);
		return $result;
	}



}
?>
