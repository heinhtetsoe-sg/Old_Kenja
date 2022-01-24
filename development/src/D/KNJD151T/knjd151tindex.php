<?php

require_once('for_php7.php');

require_once('knjd151tModel.inc');
require_once('knjd151tQuery.inc');

class knjd151tController extends Controller
{
    public $ModelClassName = "knjd151tModel";
    public $ProgramID      = "KNJD151T";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd151t":
                    $sessionInstance->knjd151tModel();
                    $this->callView("knjd151tForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd151tForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd151tCtl = new knjd151tController();
