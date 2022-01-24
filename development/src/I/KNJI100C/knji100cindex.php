<?php

require_once('for_php7.php');

require_once('knji100cModel.inc');
require_once('knji100cQuery.inc');

class knji100cController extends Controller {
    var $ModelClassName = "knji100cModel";
    var $ProgramID      = "KNJI100C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knji100c":
                    $sessionInstance->knji100cModel();      //コントロールマスタの呼び出し
                    $this->callView("knji100cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knji100cCtl = new knji100cController;
?>
