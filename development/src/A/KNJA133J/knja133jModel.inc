<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja133jModel extends Model
{
    public $field          = array();
    public $select_data    = array();
    public $cmd;
    public $control;       //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    public function knja133jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力選択
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "seito"            =>  VARS::post("seito"),            //帳票種別（生徒指導要録）
                             "simei"            =>  VARS::post("simei"),            //出力選択（生徒・保護者氏名出力）
                             "schzip"           =>  VARS::post("schzip"),           //出力選択（生徒郵便番号）
                             "schoolzip"        =>  VARS::post("schoolzip"),        //出力選択（学校郵便番号）
                             "color_print"      =>  VARS::post("color_print"),      //出力選択（カラープリンター）
                             "inei_print"       =>  VARS::post("inei_print"),       //出力選択（担任印影）
                             "inei_print2"      =>  VARS::post("inei_print2"),      //出力選択（校長・担任印影）
                             "INEI"             =>  VARS::post("INEI"),             //帳票種別(印影出力)(画面上からは削除)
                             "gakushu"          =>  VARS::post("gakushu"),          //帳票種別（学習の記録）
                             "koudo"            =>  VARS::post("koudo"),            //帳票種別（行動の記録）
                             "online"           =>  VARS::post("online"),           //帳票種別（特例の授業等の記録）
                             "PRINT_BLANK_PAGE" =>  VARS::post("PRINT_BLANK_PAGE"),      //奇数ページの時は空白ページを印刷する
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

        knja133jQuery::getInsertSql($db, $this);

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
