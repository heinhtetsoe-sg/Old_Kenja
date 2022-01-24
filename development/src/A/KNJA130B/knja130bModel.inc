<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja130bModel extends Model {
    var $field = array();
    var $selectdata = array();
    var $cmd;
    var $control;    //コントロールマスタ
    var $staffcd;    //職員コード
    var $radio;

    /**
     * メンバ変数の初期化
     */
    function knja130bModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //職員コード
    }

    function init() {
        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SEITO"            =>  VARS::post("SEITO"),            //帳票種別(生徒指導要録)
                             "KOSEKI"           =>  VARS::post("KOSEKI"),           //帳票種別(戸籍氏名出力)
                             "SIMEI"            =>  VARS::post("SIMEI"),            //帳票種別(生徒･保護者氏名出力)
                             "INEI"             =>  VARS::post("INEI"),             //帳票種別(印影出力)(画面上からは削除)
                             "COLOR_PRINT"      =>  VARS::post("COLOR_PRINT"),      //帳票種別(カラープリンター使用)
                             "SCHZIP"           =>  VARS::post("SCHZIP"),           //帳票種別(生徒郵便番号)
                             "ADDR2"            =>  VARS::post("ADDR2"),            //帳票種別(方書き)
                             "SCHOOLZIP"        =>  VARS::post("SCHOOLZIP"),        //帳票種別(学校郵便番号)
                             "KATSUDO"          =>  VARS::post("KATSUDO"),          //帳票種別(活動の記録)
                             "GAKUSHU"          =>  VARS::post("GAKUSHU"),          //帳票種別(学習の記録)
                             "TANI"             =>  VARS::post("TANI"),             //帳票種別(習得単位の記録)
                             "MIRISYU"          =>  VARS::post("MIRISYU"),          //未履修科目出力
                             "RISYU"            =>  VARS::post("RISYU"),            //履修のみ科目出力
                             "RISYUTOUROKU"     =>  VARS::post("RISYUTOUROKU"),     //履修登録のみ科目出力
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->selectdata = VARS::post("selectdata");
        $this->inei = VARS::request("INEI");

        $this->radio = VARS::post("RADIO");
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    function &getUpdateModel() {
        $db = Query::dbCheckOut();

        if (!$this->field["INEI"]) {
            return;
        }

        knja130bQuery::getInsertSql($db, $this);

        Query::dbCheckIn($db);
        return;
    }

    //ハッシュ値を作る
    function makeHash($Row, $study, $attend) {

        $data = $Row["TOTALSTUDYACT"].$Row["TOTALSTUDYVAL"].$Row["SPECIALACTREMARK"].$Row["TOTALREMARK"].$Row["ATTENDREC_REMARK"].
                $study.$attend["CLASSDAYS"].$attend["OFFDAYS"].$attend["ABSENT"].$attend["SUSPEND"].$attend["MOURNING"].
                $attend["ABROAD"].$attend["REQUIREPRESENT"].$attend["SICK"].$attend["ACCIDENTNOTICE"].$attend["NOACCIDENTNOTICE"].$attend["PRESENT"];

        return sha1($data);
    }
}
?>
