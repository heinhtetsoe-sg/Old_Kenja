<?php

require_once('for_php7.php');

require_once('knjl364kModel.inc');
require_once('knjl364kQuery.inc');

class knjl364kController extends Controller
{
    public $ModelClassName = "knjl364kModel";
    public $ProgramID      = "KNJL364K";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl364kForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl364kForm1");
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
$knjl364kCtl = new knjl364kController();
