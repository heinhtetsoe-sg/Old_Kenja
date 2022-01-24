<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja133kModel extends Model
{
    public $field          = array();
    public $select_data    = array();
    public $cmd;
    public $control;       //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    public function knja133kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力選択
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "seito"            =>  VARS::post("seito"),            //帳票種別（幼児指導要録）
                             "simei"            =>  VARS::post("simei"),            //出力選択（幼児・保護者氏名出力）
                             "schzip"           =>  VARS::post("schzip"),           //出力選択（幼児郵便番号）
                             "schoolzip"        =>  VARS::post("schoolzip"),        //出力選択（学校郵便番号）
                             "sidou"            =>  VARS::post("sidou"),            //帳票種別（指導の記録）
                             "BIRTHDAY_FORMAT"  =>  VARS::post("BIRTHDAY_FORMAT"),  //生年月日切替
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->select_data = VARS::post("selectdata");

        $this->inei = VARS::request("INEI");
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();  //プロパティファイルの読込み
    }

    public function &getUpdateModel()
    {
        $db = Query::dbCheckOut();

        if (!$this->field["INEI"]) {
            return;
        }

        knja133kQuery::getInsertSql($db, $this);

        Query::dbCheckIn($db);
        return;
    }

    //ハッシュ値を作る
    public function makeHash($tRow, $behavior, $study, $attend)
    {
        $data = $tRow["TOTALREMARK"].$tRow["TOTALSTUDYACT"].$tRow["VIEWREMARK"].$tRow["TOTALSTUDYVAL"].$tRow["ATTENDREC_REMARK"].
                $behavior.$tRow["SPECIALACTREMARK"].$study.$attend["CLASSDAYS"].$attend["OFFDAYS"].$attend["ABSENT"].
                $attend["SUSPEND"].$attend["MOURNING"].$attend["ABROAD"].$attend["REQUIREPRESENT"].$attend["SICK"].
                $attend["ACCIDENTNOTICE"].$attend["NOACCIDENTNOTICE"].$attend["PRESENT"];

        return sha1($data);
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
