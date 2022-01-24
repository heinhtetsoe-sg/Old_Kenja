<?php

require_once('for_php7.php');

require_once('knji100bModel.inc');
require_once('knji100bQuery.inc');
//区分に関しての出力設定
define("OUT_CODE_NAME", 0);
define("OUT_CODE_ONLY", 1);
define("OUT_NAME_ONLY", 2);

class knji100bController extends Controller {
    var $ModelClassName = "knji100bModel";
    var $ProgramID      = "KNJI100B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knji100bForm1");
                    break 2;
                case "csv":
                    if (!$sessionInstance->getCsvModel()){
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("edit");
                        break 1;
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $programpath = $sessionInstance->getProgrampathModel();
                    //分割フレーム作成
                    if ($programpath == ""){
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&CHECK=ON&PATH=" .urlencode("/I/KNJI100B/knji100bindex.php?cmd=edit") ."&button=3" ."&SEND_AUTH={$sessionInstance->auth}";
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&CHECK=ON&PATH=" .urlencode($programpath."/knji100bindex.php?cmd=edit") ."&button=3" ."&SEND_AUTH={$sessionInstance->auth}";
                    }
                    $args["right_src"] = "knji100bindex.php?cmd=edit";
                    $args["cols"] = "50%,*";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knji100bCtl = new knji100bController;
?>
