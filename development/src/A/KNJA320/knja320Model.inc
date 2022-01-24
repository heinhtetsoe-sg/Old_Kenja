<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja320Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knja320Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("STAFF_TELNO"      =>  VARS::post("STAFF_TELNO"),
                             "TEL_NO"           =>  VARS::post("TEL_NO"),
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS")
                            );

        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    //ＣＳＶ出力
    function getDownloadModel()
    {
        $db  = Query::dbCheckOut();

        $hrname = $db->getOne(knja320Query::getHrName($this));//---2005.06.10Add
        //ヘッダ
        $contents = "出席番号,年度,学年,組,氏名,現住所電話番号,電話番号２\n";

        $data = false;
        $result = $db->query(knja320Query::getOutputQueryCsv($this));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
	        $contents .= implode($row, ",") ."\n";
            $data = true;
        }
        Query::dbCheckIn($db);

		if ($data)
		{
            $filename = sprintf("%s.csv","緊急連絡網");//---2005.06.10Modify---2005.07.27Modify
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
		}
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
