<?php

require_once('for_php7.php');

require_once('knjl141Model.inc');
require_once('knjl141Query.inc');

class knjl141Controller extends Controller
{
    public $ModelClassName = "knjl141Model";
    public $ProgramID      = "KNJL141";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl141Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl141Form1");
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
$knjl141Ctl = new knjl141Controller();
