<?php

require_once('for_php7.php');

require_once('knjx_c162Model.inc');
require_once('knjx_c162Query.inc');

class knjx_c162Controller extends Controller
{
    public $ModelClassName = "knjx_c162Model";
    public $ProgramID      = "KNJX_C162";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjx_c162Form1");
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
$knjx_c162Ctl = new knjx_c162Controller();
