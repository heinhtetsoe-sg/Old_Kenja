<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja130gModel extends Model {
    var $field      = array();
    var $select_data    = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knja130gModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //処理年月日
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力選択  //NO002
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "seito"            =>  VARS::post("seito"),            //帳票種別（生徒指導要録）
                             "simei"            =>  VARS::post("simei"),            //帳票種別（生徒・保護者氏名出力）
                             "schzip"           =>  VARS::post("schzip"),           //帳票種別（生徒郵便番号）
                             "addr2"            =>  VARS::post("addr2"),            //帳票種別（方書き）
                             "schoolzip"        =>  VARS::post("schoolzip"),        //帳票種別（学校郵便番号）
                             "tani"             =>  VARS::post("tani"),             //帳票種別（修得単位の記録）
                             "zenki_gakushu"    =>  VARS::post("zenki_gakushu"),    //帳票種別（学習の記録）
                             "zenki_katsudo"    =>  VARS::post("zenki_katsudo"),    //帳票種別（活動の記録）
                             "kouki_gakushu"    =>  VARS::post("kouki_gakushu"),    //帳票種別（学習の記録）
                             "kouki_katsudo"    =>  VARS::post("kouki_katsudo"),    //帳票種別（活動の記録）
                             "MIRISYU"          =>  VARS::post("MIRISYU"),          //未履修科目出力
                             "RISYU"            =>  VARS::post("RISYU"),            //履修のみ科目出力
                             "RISYUTOUROKU"     =>  VARS::post("RISYUTOUROKU"),     //履修登録のみ科目出力
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->inei = VARS::request("INEI");
        $this->cmd  = VARS::request("cmd");
        $this->getPropertiesAll();

        //初期値の設定
        $this->Properties["useSchregRegdHdat"]                        = $this->Properties["useSchregRegdHdat"]                        ? $this->Properties["useSchregRegdHdat"]                        : 0;
        $this->Properties["seitoSidoYorokuFormType"]                  = $this->Properties["seitoSidoYorokuFormType"]                  ? $this->Properties["seitoSidoYorokuFormType"]                  : 4;
        $this->Properties["seitoSidoYorokuFieldSize"]                 = $this->Properties["seitoSidoYorokuFieldSize"]                 ? $this->Properties["seitoSidoYorokuFieldSize"]                 : 0;
        $this->Properties["seitoSidoYorokuSougouFieldSize"]           = $this->Properties["seitoSidoYorokuSougouFieldSize"] == '1'    ? '1'                                                           : 0;
        $this->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] = $this->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == '1' ? '1'                                                    : 0;
        $this->Properties["seitoSidoYorokuKinsokuForm"]               = $this->Properties["seitoSidoYorokuKinsokuForm"]               ? $this->Properties["seitoSidoYorokuKinsokuForm"]               : 0;
    }

    function &getUpdateModel() {
        $db = Query::dbCheckOut();

        if (!$this->inei) {
            return;
        }

        knja130gQuery::getInsertSql($db, $this);

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

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
