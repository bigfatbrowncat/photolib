function createImageTable(output) {
    output.clearImages = function() {
		while (output.firstChild) {
			output.removeChild(output.firstChild);
		}				
	}
	
    output.processUpdateRequest = function (path_items, arguments) {
	
    	if (path_items[0] == "add") {
    		var image = arguments[0];
    		this.appendItem(image);
    		return true;
    	} else if (path_items[0] == "remove") {
    		var index = arguments[0];
    		this.remove(index);
    		return true;
    	} else if (path_items[0] == "item") {
		    path_items = path_items.splice(1);
	    	var imageId = 'images[' + path_items[0] + ']';
		    path_items = path_items.splice(1);
		    var imageTag = document.getElementById(imageId);
			
		    return imageTag.processUpdateRequest(path_items, arguments);
	    }
	}
    
	output.createItem = function(index, image) {
		var imgId = "images[" + index + "]";
		
		var picImgTag = document.createElement("img");
		picImgTag.setAttribute("id", imgId + ".picture");
		picImgTag.setAttribute("class", "image-picture");
		
		picImgTag.setFile = function(file) {
			this.file = file;
			this.setAttribute("src", "image/" + file.name);
		};
		picImgTag.getFile = function() {
			return this.file;
		}

		if (image.files.length > 0) {
			var file = image.files[0];
			picImgTag.setFile(file);
		}

		var deleteButtonTag = document.createElement("button");
		deleteButtonTag.setAttribute("id", imgId + ".deleteButton");
		deleteButtonTag.setAttribute("class", "image-deleteButton");
		deleteButtonTag.innerHTML = "Erase";
		deleteButtonTag.addEventListener("click", function() {
			websocket.send(JSON.stringify({
					"command": {
						"items": ["images", "remove"]
					},
					"arguments": [index]
				}
			));
		});
		
		var titleTag = document.createElement("span");
		titleTag.setAttribute("id", imgId + ".title");
		titleTag.setAttribute("contenteditable", "true");
		titleTag.setAttribute("class", "image-title");
		titleTag.innerHTML = image.title;
		
		titleTag.addEventListener("focus", function() {
			imageTag.selectItem();
		});
		titleTag.addEventListener("input", function() {
			websocket.send(JSON.stringify(
				{
					"command": {
						"items": ["image", index, "title"] 
					},
					"arguments": [titleTag.innerHTML]
				}
			));
		});
		
		var titleContainerTag = document.createElement("div");
		titleContainerTag.setAttribute("id", imgId + ".titlecontainer");
		titleContainerTag.setAttribute("class", "image-title-container");
		titleContainerTag.appendChild(titleTag);


		var imageTag = document.createElement("div");
		imageTag.setAttribute("tabindex", index);	// TODO Calculate correct tabindex
		imageTag.setAttribute("id", imgId);
		imageTag.setAttribute("class", "image");
				
		imageTag.appendChild(picImgTag);
		imageTag.appendChild(titleContainerTag);
		imageTag.appendChild(deleteButtonTag);
		
		imageTag.selectItem = function() {
			if (selectedImage != null) {
				selectedImage.classList.remove("selected");
			}
			selectedImage = imageTag;
			imageTag.classList.add("selected");
		}
		
		imageTag.addEventListener("focus", function() {
			imageTag.selectItem();
		});
		
		imageTag.processUpdateRequest = function (path_items, arguments) {
			if (path_items[0] == 'files') {
			    path_items = path_items.splice(1);
			    
		    	if (path_items[0] == "add") {
			    	var picture = document.getElementById(imageTag.id + ".picture");
			    	if (picture == null) {
			    		throw "Image with id '" + imageTag.id + ".picture" + "' not found on the page";
			    	}
		    		var file = arguments[0];
					picture.setFile(file);
		    		return true;
		    	} else if (path_items[0] == "item") {
		    		
		    		// Nothing here yet
		    		
		    	}
 
		    } else if (path_items[0] == 'title') {
		    	var title = document.getElementById(imageTag.id + ".title");
		    	if (title.innerHTML != arguments[0]) {
		    		title.innerHTML = arguments[0];
		    	}
		    	return;
		    }
		}
		
		// Raising the image when its picture has loaded
		picImgTag.addEventListener("load", function() {
			picImgTag.classList.add("raised");
		});

		// Animating the newcomer		
		setTimeout(function() {
			imageTag.classList.add("raised");
		}, 10);
		
		return imageTag;
	}
	
	output.appendItem = function(image) {
		var imageTag = this.createItem(image.id, image);
		this.appendChild(imageTag);
	}
	
	output.appendItems = function(imageList) {
		for (var index in imageList.images) {
			var image = imageList.images[index];
			this.appendItem(image);									
		}
	}
	
	output.remove = function (index) {
		var imgId = "images[" + index + "]";
	    var image = document.getElementById(imgId);

		this.removeChild(image);
	}
}