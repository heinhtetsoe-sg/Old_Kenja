<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjf030gModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $select_opt  = array(); //セレクトデータ
    var $selectleft;

    /**
     * メンバ変数の初期化
     */
    function knjf030gModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init() {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                            "GAKKI"             =>  VARS::post("GAKKI"),            //学期コード
                            "KUBUN"             =>  VARS::post("KUBUN"),            //クラス・個人表示指定
                            "GRADE_HR_CLASS"    =>  VARS::post("GRADE_HR_CLASS"),   //クラス
                            "DATE"              =>  VARS::post("DATE"),             //作成日
                            "PRESENT_DATE"      =>  VARS::post("PRESENT_DATE"),     //提出日
                            "INSPECTION_FROM"   =>  VARS::post("INSPECTION_FROM"),  //検査日時from
                            "INSPECTION_TO"     =>  VARS::post("INSPECTION_TO"),    //検査日時to
                            "INSPECTION_BODY"   =>  VARS::post("INSPECTION_BODY"),  //検査機関
                            //共通
                            "CHECK1"            =>  VARS::post("CHECK1"),           //健康診断票（一般）
                            "CHECK2"            =>  VARS::post("CHECK2"),           //健康診断票（一般）写し
                            "CHECK3"            =>  VARS::post("CHECK3"),           //健康診断票（歯・口腔）
                            //中学
                            "CHECK4"            =>  VARS::post("CHECK4"),           //健康診断結果のお知らせ
                            "CHECK5"            =>  VARS::post("CHECK5"),           //受診報告書
                            "CHECK6"            =>  VARS::post("CHECK6"),           //検診のおすすめ票（眼科）
                            "CHECK7"            =>  VARS::post("CHECK7"),           //検診のおすすめ票（歯科）
                            "CHECK8"            =>  VARS::post("CHECK8"),           //検診のおすすめ票（耳鼻科）
                            "CHECK9"            =>  VARS::post("CHECK9"),           //検診のおすすめ票（心臓）
                            "CHECK10"           =>  VARS::post("CHECK10"),          //検診のおすすめ票（内科）
                            "CHECK11"           =>  VARS::post("CHECK11"),          //検診のおすすめ票（尿）
                            "CHECK12"           =>  VARS::post("CHECK12"),          //心臓検診結果のお知らせ
                            "CHECK13"           =>  VARS::post("CHECK13"),          //腎臓検診第2次（尿検査）のお知らせ
                            "CHECK14"           =>  VARS::post("CHECK14"),          //尿検査の結果について
                            "CHECK15"           =>  VARS::post("CHECK15"),          //治療継続証明書
                            //高校
                            "CHECK16"           =>  VARS::post("CHECK16"),          //健康診断結果のお知らせ
                            "CHECK17"           =>  VARS::post("CHECK17"),          //受診報告書
                            "CHECK18"           =>  VARS::post("CHECK18"),          //検診のおすすめ票（眼科）
                            "CHECK19"           =>  VARS::post("CHECK19"),          //検診のおすすめ票（歯科）
                            "CHECK20"           =>  VARS::post("CHECK20"),          //検診のおすすめ票（耳鼻科）
                            "CHECK21"           =>  VARS::post("CHECK21"),          //検診のおすすめ票（心臓）
                            "CHECK22"           =>  VARS::post("CHECK22"),          //検診のおすすめ票（内科）
                            "CHECK23"           =>  VARS::post("CHECK23"),          //検診のおすすめ票（尿）
                            "CHECK24"           =>  VARS::post("CHECK24"),          //検診のおすすめ票（結核）
                            "CHECK25"           =>  VARS::post("CHECK25"),          //心臓検診結果のお知らせ
                            "CHECK26"           =>  VARS::post("CHECK26"),          //腎臓検診第2次（尿検査）のお知らせ
                            "CHECK27"           =>  VARS::post("CHECK27"),          //尿検査の結果について
                            "CHECK28"           =>  VARS::post("CHECK28"),          //結核検診の結果について
                            "CHECK29"           =>  VARS::post("CHECK29"),          //治療継続証明書
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        if ($this->cmd != 'change_class') $this->select_opt = array();

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
