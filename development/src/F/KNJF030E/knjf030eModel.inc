<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjf030eModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $select_opt  = array(); //セレクトデータ
    var $selectleft;

    /**
     * メンバ変数の初期化
     */
    function knjf030eModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                            "GAKKI"                 =>  VARS::post("GAKKI"),                //学期コード
                            "KUBUN"                 =>  VARS::post("KUBUN"),                //クラス・個人表示指定
                            "GRADE_HR_CLASS"        =>  VARS::post("GRADE_HR_CLASS"),       //クラス
                            "GANKA_KENSIN"          =>  VARS::post("GANKA_KENSIN"),         //眼科受診チェックボックス
                            "KEKKA_HA"              =>  VARS::post("KEKKA_HA"),             //検診結果チェックボックス
                            "KEKKA_HA_CARD"         =>  VARS::post("KEKKA_HA_CARD"),        //治療済票付きチェックボックス
                            "KEKKA_HA2"             =>  VARS::post("KEKKA_HA2"),            //歯・口の健康診断結果チェックボックス
                            "KEKKA_HA2_DATE"        =>  VARS::post("KEKKA_HA2_DATE"),       //歯・口の健康診断結果（作成日付）
                            "TEIKI_KENSIN"          =>  VARS::post("TEIKI_KENSIN"),         //定期健康診断チェックボックス
                            "TEIKI_KENSIN_DATE"     =>  VARS::post("TEIKI_KENSIN_DATE"),    //定期健康診断（作成日付）
                            "HEART_MEDEXAM_PRINT"   =>  VARS::post("HEART_MEDEXAM_PRINT"),  //心電図検査出力チェックボックス
                            "TB_PRINT"              =>  VARS::post("TB_PRINT"),             //結核検査(X線)出力チェックボックス
                            "TEIKI_KENSIN_ITIRAN"   =>  VARS::post("TEIKI_KENSIN_ITIRAN"),  //定期健康診断一覧チェックボックス
                            "DBNAME"                =>  VARS::post("DBNAME")                //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        if ($this->cmd != 'change_class') $this->select_opt = array();

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->printKenkouSindanIppan = ($this->Properties["printKenkouSindanIppan"] == '1') ? $this->Properties["printKenkouSindanIppan"] : 0;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
