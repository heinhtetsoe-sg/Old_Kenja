/**
 *
 */
	function codec(){
		ra = new Array("0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f");
		r = "";
		for(var i=0;i<20;i++) {
			rnd = Math.floor( Math.random() * 16 );
			r += ra[rnd];
		}
		var shaObj = new jsSHA(r+document.form.pass.value, 'ASCII');
		var sha1digest = shaObj.getHash("SHA-1", "HEX");
		document.form.token.value = r + sha1digest;
		document.form.pass.value = "";
	}
