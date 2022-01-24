<?php

require_once('for_php7.php');

require_once('knjm430Model.inc');
require_once('knjm430Query.inc');

class knjm430Controller extends Controller {
    var $ModelClassName = "knjm430Model";
    var $ProgramID      = "KNJM430";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "change":				//科目（講座）が変わったとき
                case "change_order":		//出力順が変わったとき
                case "change_class":		//講座受講クラスが変わったとき
                case "reset":
                case "read":
                    $sessionInstance->getMainModel();
                    $this->callView("knjm430Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm430Ctl = new knjm430Controller;
?>
