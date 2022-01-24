<?php

require_once('for_php7.php');

require_once('knjb3043Model.inc');
require_once('knjb3043Query.inc');

class knjb3043Controller extends Controller {
    var $ModelClassName = "knjb3043Model";
    var $ProgramID      = "KNJB3043";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":            // 読込ボタン
                case "editSchDiv":      // 年度学期/展開表種別(科目・講座)/テンプレートタイトル 変更
                case "editCmb":         // 行タイトル(教育課程・クラス)

                // AJAX用
                case "getLayoutCourse":         // 科目展開表 レイアウト編集(縦)
                case "getSubclass":             // 科目情報取得(設定科目一覧のコンボボックス変更時)
                case "getChair":                // 講座情報取得(設定講座一覧のコンボボックス変更時)
                case "getLayoutCourseChair":    // 講座展開表 レイアウト編集(縦)

                case "getCourseChair":          // 講座展開表 講座からコースの取得(反映押下時処理)

                case "getStdDupCnt":            // 講座展開表 重複人数チェック
                case "getStdUnPlacedCnt":       // 講座展開表 未配置人数チェック
                case "getFacCapOverCnt":        // 講座展開表 施設講座キャパ超チェック

                case "getBackColorChairCapaOver":   // 講座展開表 背景色変更(講座人数オーバー)
                case "getBackColorChair":           // 講座展開表 背景色変更(指定科目・講座 科目変更時)
                case "getBackColorChairSameMeibo":  // 講座展開表 背景色変更(同一名簿取得)
                case "getBackColorStdChair":        // 講座展開表 背景色変更(指定生徒の受講講座)
                case "getBackColorHrClassStdMeibo": // 講座展開表 背景色変更(指定生徒の受講講座 年組変更時)

                case "reset":
                    $this->callView("knjb3043Form1");
                    break 2;
                case "update":  // 科目展開表更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "preSeqDelete":        // 科目展開表テンプレート削除
                case "preChairSeqDelete":   // 講座展開表テンプレート削除
                    $sessionInstance->deletePreSeqModel();
                    $sessionInstance->setCmd("");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }            
        }
    }

}
$knjb3043Ctl = new knjb3043Controller;
//var_dump($_REQUEST);
?>
