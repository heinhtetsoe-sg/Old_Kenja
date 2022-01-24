<?php

require_once('for_php7.php');

class knjd181aModel extends Model {
    var $semester;
    var $hrClass;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ
    var $tutisyoTokubetuKatudo;
    var $useSlumpD048;

    function knjd181aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->hrClass  = VARS::post("GRADE_HR_CLASS");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
        }

        $this->field = array("SEMESTER"               =>  VARS::post("SEMESTER"),               //学期
                             "HR_CLASS_TYPE"          => VARS::post("HR_CLASS_TYPE"),           //クラス方式
                             "GAKUNEN_KONGOU"         => VARS::post("GAKUNEN_KONGOU"),          //学年混合チェックボックス
                             "DESC_DATE"              =>  VARS::post("DESC_DATE"),              //記載日付
                             "PATTERN"                =>  VARS::post("PATTERN"),                //帳票パターンラジオボタン
                             "TYOUHYOU_PATTERN"       =>  VARS::post("TYOUHYOU_PATTERN"),       //帳票パターンラジオボタン
                             "NO_PRINT_STUDENT_NAME"  =>  VARS::post("NO_PRINT_STUDENT_NAME"),  //生徒名出力なし
                             "NO_PRINT_STUDENT_NAME2" =>  VARS::post("NO_PRINT_STUDENT_NAME2"), //生徒名出力なし2
                             "OKURIJOU_JUSYO"         =>  VARS::post("OKURIJOU_JUSYO"),         //送り状住所ラジオボタン
                             "FORM_SELECT"            =>  VARS::post("FORM_SELECT"),            //A4角形封筒
                             "JUSYO_PRINT"            =>  VARS::post("JUSYO_PRINT"),            //送り状住所なし
                             "SYUSEKI"                =>  VARS::post("SYUSEKI"),                //(SHRの欠席時数　遅刻・早退)チェックボックス
                             "KOUTYOU"                =>  VARS::post("KOUTYOU"),                //校調印の出力あり
                             "SPECIALACT"             =>  VARS::post("SPECIALACT"),             //特別活動の出力方法ラジオボタン
                             "SHR_LATE_EARLY"         =>  VARS::post("SHR_LATE_EARLY"),         //(SHRの欠席時数　遅刻・早退)ラジオボタン
                             "LATE_EARLY"             =>  VARS::post("LATE_EARLY"),             //遅刻・早退出力ありチェックボックス
                             "HANREI_SYUTURYOKU_NASI" =>  VARS::post("HANREI_SYUTURYOKU_NASI"), //凡例出力なし
                             "TYUI_TYOUKA_CHECK"      =>  VARS::post("TYUI_TYOUKA_CHECK"),      //注意・超過チェックボックス
                             "TYUI_TYOUKA"            =>  VARS::post("TYUI_TYOUKA"),            //注意・超過ラジオボタン
                             "GAKUNENMATU_RAN"        =>  VARS::post("GAKUNENMATU_RAN"),        //学年末欄の内容ラジオボタン
                             "SYUKKETU_NO_KIROKU_SOUGOUTEKI_NA_GAKUSYUU_NO_JIKAN_NO_RAN_NASI" =>  VARS::post("SYUKKETU_NO_KIROKU_SOUGOUTEKI_NA_GAKUSYUU_NO_JIKAN_NO_RAN_NASI"),   //出欠の記録、総合的な学習の時間の欄なし チェックボックス
                             "KETTEN"                 =>  VARS::post("KETTEN"),                 //欠点（評価）
                             "KETTEN_HYOTEI"          =>  VARS::post("KETTEN_HYOTEI"),          //欠点（評定）
                             "SCHOOL_KIND"            =>  VARS::post("SCHOOL_KIND"),            //学校区分
                             "ZOUKA"                  =>  VARS::post("ZOUKA"),                  //増加単位を反映させる
                             "TR_PRINT"               =>  VARS::post("TR_PRINT")                //担任印出力
                             );

        if ($this->cmd == '') {
            $this->field["ZOUKA"] = '1';
        }

        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み

        //「欠点(評価)は、不振チェック参照するか？」のフラグを取得
        $rtnRow = knjd181aQuery::getNameMstD048();
        $this->useSlumpD048 = $rtnRow["NAMESPARE1"];
    }
}
?>
