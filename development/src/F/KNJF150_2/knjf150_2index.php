<?php

require_once('for_php7.php');

require_once('knjf150_2Model.inc');
require_once('knjf150_2Query.inc');

class knjf150_2Controller extends Controller
{
    public $ModelClassName = "knjf150_2Model";
    public $ProgramID      = "KNJF150";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":  //更新
                    $sessionInstance->setAccessLogDetail("U", "KNJF150_2");
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("schno");
                    break 1;
                case "delete":  //削除
                    $sessionInstance->setAccessLogDetail("D", "KNJF150_2");
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("schno");
                    break 1;
                case "":
                case "schno":
                    $sessionInstance->setAccessLogDetail("S", "KNJF150_2");
                    $sessionInstance->knjf150_2Model();     //コントロールマスタの呼び出し
                    $this->callView("knjf150_2Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf150_2Ctl = new knjf150_2Controller();
//var_dump($_REQUEST);
