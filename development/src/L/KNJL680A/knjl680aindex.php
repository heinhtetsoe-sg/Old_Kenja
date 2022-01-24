<?php

require_once('for_php7.php');

require_once('knjl680aModel.inc');
require_once('knjl680aQuery.inc');

class knjl680aController extends Controller
{
    public $ModelClassName = "knjl680aModel";
    public $ProgramID      = "KNJL680A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl680aForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl680aForm1");
                    break 2;
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
$knjl680aCtl = new knjl680aController;
?>
