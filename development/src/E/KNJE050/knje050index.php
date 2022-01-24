<?php

require_once('for_php7.php');

require_once('knje050Model.inc');
require_once('knje050Query.inc');

class knje050Controller extends Controller {
    var $ModelClassName = "knje050Model";
    var $ProgramID      = "KNJE050";

    function main()
    {
        $sessionInstance =& Model::getModel($this);

        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "sort":
                    $sessionInstance->getMainModel();
                    $this->callView("knje050Form2");
                    break 2;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje050Form2");
                    }
                    break 2;
                case "left":
                    $this->callView("knje050Form1");
                    break 2;
                case "subform1": //成績参照
                    $this->callView("knje050SubForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]   = "knje050index.php?cmd=left";
                    $args["right_src"]  = "knje050index.php?cmd=main";
                    $args["cols"] = "30%,*";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knje050Ctl = new knje050Controller;
?>
